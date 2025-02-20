package com.example.supersmartkeyapp.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.SuperSmartKeyActivity
import com.example.supersmartkeyapp.admin.DeviceAdmin
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
private const val ACTION_STOP_LOCK_SERVICE = "STOP_LOCK_SERVICE"
private const val CHANNEL_ID = "SuperSmartKeyChannelId"
private const val CHANNEL_NAME = "SuperSmartKeyChannel"
private const val SERVICE_ID = 426

@AndroidEntryPoint
class KeyService : Service(), DefaultLifecycleObserver {
    @Inject
    lateinit var settingsRepository: ServiceRepository

    @Inject
    lateinit var keyRepository: KeyRepository

    private lateinit var wakeReceiver: BroadcastReceiver
    private val binder = KeyBinder()
    private var rssiPollingJob: Job? = null
    private var gracePeriodJob: Job? = null
    private val updateScope = CoroutineScope(Dispatchers.IO + Job())
    private val collectScope = CoroutineScope(Dispatchers.IO + Job())

    private var settings = Settings(
        rssiThreshold = DEFAULT_RSSI_THRESHOLD,
        gracePeriod = DEFAULT_GRACE_PERIOD,
        pollingRateSeconds = DEFAULT_POLLING_RATE
    )
    private var isLockServiceRunning = false
    private var isGracePeriod = false
    private var isAppForeground = true


    inner class KeyBinder : Binder() {
        fun getService(): KeyService = this@KeyService
    }

    companion object {
        var isRunning = false
    }

    override fun onCreate() {
        super<Service>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        wakeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_USER_PRESENT -> if (isLockServiceRunning) startGracePeriod()
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(wakeReceiver, filter)
        isRunning = true
    }

    override fun onDestroy() {
        super<Service>.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        unregisterReceiver(wakeReceiver)
        stopRssiPolling()
        stopGracePeriod()
        if (isLockServiceRunning) {
            updateScope.launch {
                settingsRepository.updateIsLockServiceRunning(false)
            }
        }
        collectScope.cancel()
        Log.d(TAG, "Key service destroyed")
        isRunning = false
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App is in the foreground")
        isAppForeground = true
        startRssiPolling()
    }

    //    TODO: Prevent polling in background if service not running
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App is in the background")
        isAppForeground = false
    }


    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    //    TODO: USE THIS FOR SOMETHING?
    override fun onTaskRemoved(intent: Intent) {
        super.onTaskRemoved(intent)
    }

    //    TODO: Figure out stopping service after noti stop when app is closed
//    TODO: Figure out weird process mangement on destroy/caached process
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_STOP_LOCK_SERVICE) {
            stopLockService()
            if (!isAppForeground) {
                stopSelf()
            }
        } else {
            collectScope.launch {
                settingsRepository.settingsFlow.collect { value ->
                    settings = value
                }
            }
            collectScope.launch {
                settingsRepository.isLockServiceRunningFlow.collect { value ->
                    isLockServiceRunning = value
                }
            }
            collectScope.launch {
                keyRepository.key.collect { value ->
                    Log.d(TAG, "RSSI: ${value?.rssi}")
                    if (value?.rssi != null && isLockServiceRunning && !isGracePeriod) {
                        if (value.rssi < settings.rssiThreshold) {
                            Log.d(TAG, "Locking device")
                            lockDevice()
                            isGracePeriod = true
                        }
                    }
                }
            }
            startRssiPolling()
        }

        return START_STICKY
    }

    fun startLockService() {
        startForeground()
    }

    fun stopLockService() {
        if (isLockServiceRunning) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopGracePeriod()
            updateScope.launch {
                settingsRepository.updateIsLockServiceRunning(false)
            }
        }
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
            updateScope.launch {
                settingsRepository.updateIsLockServiceRunning(true)
            }
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                Log.e(TAG, e.toString())
            }
        }
    }

    private fun createNotification(): Notification {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val stopIntent = Intent(this, KeyService::class.java).apply {
            action = ACTION_STOP_LOCK_SERVICE
        }
        val stopPendingIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val mainIntent = Intent(this, SuperSmartKeyActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent =
            PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.icon)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text)).setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(mainPendingIntent)
                .addAction(
                    R.drawable.stop, getString(R.string.stop), stopPendingIntent
                )

        return notification.build()
    }

    private fun startRssiPolling() {
        if (rssiPollingJob == null) {
            rssiPollingJob = CoroutineScope(Dispatchers.IO).launch {
                while (isActive && (isLockServiceRunning || isAppForeground)) {
                    keyRepository.readRemoteRssi()
//                Log.d(TAG, "Threshold: ${settings.rssiThreshold}")
//                Log.d(TAG, "Grace: ${settings.gracePeriod}")
//                Log.d(TAG, "Polling Rate: ${settings.pollingRateSeconds}")
                    val pollingRateInMillis = settings.pollingRateSeconds * 1000.toLong()
                    delay(pollingRateInMillis)
                }
                rssiPollingJob = null
            }
        }
    }

    private fun stopRssiPolling() {
        rssiPollingJob?.cancel()
        keyRepository.disconnectKey()
        rssiPollingJob = null
    }

    private fun startGracePeriod() {
        if (gracePeriodJob == null) {
            gracePeriodJob = CoroutineScope(Dispatchers.IO).launch {
                val gracePeriodInMillis = settings.gracePeriod * 1000.toLong()
                delay(gracePeriodInMillis)
                isGracePeriod = false
                gracePeriodJob = null
            }
        }
    }

    private fun stopGracePeriod() {
        gracePeriodJob?.cancel()
        gracePeriodJob = null
        isGracePeriod = false
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