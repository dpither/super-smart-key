/*
    Copyright (C) 2025  Dylan Pither

    This file is part of Super Smart Key.

    Super Smart Key is free software: you can redistribute it and/or modify it under the terms of
    the GNU General Public License as published by the Free Software Foundation, either version 3
    of the License, or (at your option) any later version.

    Super Smart Key is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with Super Smart Key.
    If not, see <https://www.gnu.org/licenses/>.
 */

package com.dpither.supersmartkey.service

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
import com.dpither.supersmartkey.R
import com.dpither.supersmartkey.SuperSmartKeyActivity
import com.dpither.supersmartkey.admin.DeviceAdmin
import com.dpither.supersmartkey.data.model.Settings
import com.dpither.supersmartkey.data.repository.KeyRepository
import com.dpither.supersmartkey.data.repository.ServiceRepository
import com.dpither.supersmartkey.util.DEFAULT_GRACE_PERIOD
import com.dpither.supersmartkey.util.DEFAULT_POLLING_RATE
import com.dpither.supersmartkey.util.DEFAULT_RSSI_THRESHOLD
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
    private var isKeyConnected = false
    private var currRssi = 0

    private var isLockServiceRunning = false
    private var isGracePeriod = false
    private var bound = false
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
//                    Broadcast when device is unlocked
                    Intent.ACTION_USER_PRESENT -> if (isLockServiceRunning) {
                        startGracePeriod()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
        }

        registerReceiver(wakeReceiver, filter)
        isRunning = true
        Log.d(TAG, "Key service created")
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

        updateScope.launch {
            keyRepository.disconnectKey()
        }

        collectScope.cancel()
        isRunning = false
        Log.d(TAG, "Key service destroyed")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppForeground = true
        startRssiPolling()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppForeground = false
    }

    override fun onBind(intent: Intent): IBinder {
        bound = true
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        bound = false
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_STOP_LOCK_SERVICE) {
            stopLockService()
            if (!bound) {
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
                        if (value.rssi < settings.rssiThreshold || !value.connected) {
                            Log.d(TAG, "RSSI value too far, attempting lock")
                            lockDevice()
                        }
                    }

                    isKeyConnected = value?.connected ?: false
                    currRssi = value?.rssi ?: 0
                }
            }
            startRssiPolling()
        }

        return START_STICKY
    }

    fun startLockService() {
        startForeground()
//        Upon starting service, if criteria is met lock device,
//        rather than waiting for next read
        if (currRssi < settings.rssiThreshold || !isKeyConnected) {
            Log.d(TAG, "Service start attempting lock")
            lockDevice()
        }
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
                .setContentIntent(mainPendingIntent).addAction(
                    R.drawable.stop, getString(R.string.stop), stopPendingIntent
                )

        return notification.build()
    }

    private fun startRssiPolling() {
        if (rssiPollingJob == null) {
            rssiPollingJob = CoroutineScope(Dispatchers.IO).launch {
                while (isActive && (isLockServiceRunning || isAppForeground)) {
                    keyRepository.requestRemoteRssi()
                    val pollingRateInMillis = settings.pollingRateSeconds * 1000.toLong()
                    delay(pollingRateInMillis)
                }
                rssiPollingJob = null
            }
        }
    }

    private fun stopRssiPolling() {
        rssiPollingJob?.cancel()
        rssiPollingJob = null
        keyRepository.disconnectKey()
    }

    private fun startGracePeriod() {
        if (gracePeriodJob == null) {
            gracePeriodJob = CoroutineScope(Dispatchers.IO).launch {
                val gracePeriodInMillis = settings.gracePeriod * 1000.toLong()
                Log.d(TAG, "Starting grace period")
                delay(gracePeriodInMillis)
                Log.d(TAG, "Grace period ended")
                isGracePeriod = false
                gracePeriodJob = null
//                    Upon grace period end, if criteria is met lock device,
//                    rather than waiting for next read
                if (isLockServiceRunning && (currRssi < settings.rssiThreshold || !isKeyConnected)) {
                    Log.d(TAG, "Grace period end attempting lock")
                    lockDevice()
                }
            }
        }
    }

    private fun stopGracePeriod() {
        gracePeriodJob?.cancel()
        gracePeriodJob = null
        isGracePeriod = false
    }

    private fun lockDevice() {
        val devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, DeviceAdmin::class.java)

        if (devicePolicyManager.isAdminActive(componentName)) {
            isGracePeriod = true
            Log.d(TAG, "Locking device")
            devicePolicyManager.lockNow()
        } else {
            Log.e(TAG, "ERROR LOCKING DEVICE: Admin permission is not active")
        }
    }
}