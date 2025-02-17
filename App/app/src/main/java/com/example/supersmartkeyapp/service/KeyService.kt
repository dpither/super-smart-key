package com.example.supersmartkeyapp.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.SuperSmartKeyActivity
import com.example.supersmartkeyapp.admin.DeviceAdmin
import com.example.supersmartkeyapp.data.model.Key
import com.example.supersmartkeyapp.data.model.Settings
import com.example.supersmartkeyapp.data.repository.KeyRepository
import com.example.supersmartkeyapp.data.repository.ServiceRepository
import com.example.supersmartkeyapp.util.DEFAULT_GRACE_PERIOD
import com.example.supersmartkeyapp.util.DEFAULT_POLLING_RATE
import com.example.supersmartkeyapp.util.DEFAULT_RSSI_THRESHOLD
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "KEY_SERVICE"
private const val ACTION_PAUSE = "PAUSE_KEY_SERVICE"
private const val CHANNEL_ID = "SuperSmartKeyChannelId"
private const val CHANNEL_NAME = "SuperSmartKeyChannel"
private const val SERVICE_ID = 426

@AndroidEntryPoint
class KeyService : Service() {
    @Inject
    lateinit var settingsRepository: ServiceRepository
    @Inject
    lateinit var keyRepository: KeyRepository

    private val binder = KeyBinder()
    private var rssiPollingJob: Job? = null
    private val updateScope = CoroutineScope(Dispatchers.IO + Job())
    private val collectScope = CoroutineScope(Dispatchers.IO + Job())

    private var settings = Settings(
        rssiThreshold = DEFAULT_RSSI_THRESHOLD,
        gracePeriod = DEFAULT_GRACE_PERIOD,
        pollingRateSeconds = DEFAULT_POLLING_RATE
    )
    private var isServiceRunning = false
    private var key: Key? = null


    inner class KeyBinder : Binder() {
        fun getService(): KeyService = this@KeyService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_PAUSE) {
            pauseService()
        } else if (!isServiceRunning) {
            collectScope.launch {
                settingsRepository.settingsFlow.collect { value ->
                    settings = value
                }
            }
            collectScope.launch {
                settingsRepository.isServiceRunningFlow.collect { value ->
                    isServiceRunning = value
                }
            }
            collectScope.launch {
                keyRepository.key.collect { value ->
                    Log.d(TAG, "RSSI: ${value?.rssi}")
                    if (value?.rssi != null && isServiceRunning) {
                        if (value.rssi < settings.rssiThreshold) {
                            Log.d(TAG, "Locking device")
                        }
                    }
                    key = value
                }
            }
            startRssiPolling()
        }

        return START_STICKY
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        stopRssiPolling()

        if (isServiceRunning) {
            updateScope.launch {
                settingsRepository.updateIsServiceRunning(false)
            }
        }
        collectScope.cancel()
        Log.d(TAG, "Key service destroyed")
    }

    fun runService() {
        updateScope.launch {
            settingsRepository.updateIsServiceRunning(true)
        }
        startForeground()
    }

    fun pauseService() {
        updateScope.launch {
            settingsRepository.updateIsServiceRunning(false)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun startForeground() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    SERVICE_ID,
                    createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                ServiceCompat.startForeground(
                    this, SERVICE_ID, createNotification(), 0
                )
            }
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                Log.e(TAG, e.toString())
            }
        }
        updateScope.launch {
            settingsRepository.updateIsServiceRunning(true)
        }
    }

    private fun createNotification(): Notification {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val stopIntent = Intent(this, KeyService::class.java).apply {
            action = ACTION_PAUSE
        }
        val stopPendingIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val mainIntent = Intent(this, SuperSmartKeyActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent =
            PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.key)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text)).setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setContentIntent(mainPendingIntent)
            .addAction(
                R.drawable.stop, getString(R.string.stop), stopPendingIntent
            )

        return notification.build()
    }

    private fun startRssiPolling() {
        if (rssiPollingJob == null) {
            rssiPollingJob = CoroutineScope(Dispatchers.IO).launch {
                while (isActive) {
                    keyRepository.readRemoteRssi()
//                Log.d(TAG, "Threshold: ${settings.rssiThreshold}")
//                Log.d(TAG, "Grace: ${settings.gracePeriod}")
//                Log.d(TAG, "Polling Rate: ${settings.pollingRateSeconds}")
                    val pollingRateInMillis = settings.pollingRateSeconds * 1000.toLong()
                    delay(pollingRateInMillis)
                }
            }
        }
    }

    private fun stopRssiPolling() {
        rssiPollingJob?.cancel()
        keyRepository.unlink()
    }

//    TODO: Move to admin maybe?
    private fun lockDevice() {
        val devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, DeviceAdmin::class.java)
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        } else {
            Log.e(TAG, "Admin permission is not active")
        }
    }
}