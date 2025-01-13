package com.example.supersmartkeyapp.service

import android.annotation.SuppressLint
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.SuperSmartKeyActivity
import com.example.supersmartkeyapp.admin.DeviceAdmin
import com.example.supersmartkeyapp.data.KeyRepository
import com.example.supersmartkeyapp.data.ServiceRepository
import com.example.supersmartkeyapp.data.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ACTION_START = "START_SERVICE"
const val ACTION_RUN = "RUN_SERVICE"
const val ACTION_STOP = "STOP_SERVICE"
const val TAG = "SSK_SERVICE"

@AndroidEntryPoint
class SuperSmartKeyService : Service() {
    @Inject
    lateinit var serviceRepository: ServiceRepository
    @Inject
    lateinit var keyRepository: KeyRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val rssiPollerScope = CoroutineScope(Dispatchers.IO + Job())
    private val bluetoothManager: BluetoothManager by lazy {
        this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private var bluetoothGatt: BluetoothGatt? = null

    companion object {
        private const val CHANNEL_ID = "SuperSmartKeyChannelId"
        private const val CHANNEL_NAME = "SuperSmartKeyChannel"
        private const val SERVICE_ID = 426
        private const val DEVICE_ADDRESS_EXTRA = "device_address_extra"
        private const val POLL_DELAY: Long = 1000

        fun startService(context: Context, deviceAddress: String) {
            val intent = Intent(context, SuperSmartKeyService::class.java).apply {
                action = ACTION_START
            }
            intent.putExtra(DEVICE_ADDRESS_EXTRA, deviceAddress)
            context.startService(intent)
        }

        fun runService(context: Context) {
            val intent = Intent(context, SuperSmartKeyService::class.java).apply {
                action = ACTION_RUN
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SuperSmartKeyService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
        val notificationManager: NotificationManager =
            getSystemService(NotificationManager::class.java)
//        TODO: REFACTOR
//        serviceScope.launch {
//            serviceRepository.updateIsKeyLinked(false)
//        }
        serviceScope.launch {
            serviceRepository.updateIsServiceRunning(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check if the action is to stop the service
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf() // Stop the service
            }
            ACTION_START -> {
//                TODO: Refactor
//                val deviceAddress = intent.getStringExtra(DEVICE_ADDRESS_EXTRA)
//                val bluetoothAdapter = bluetoothManager.adapter
//                try {
//                    val bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
//                    Log.d(TAG, "Target Device: ${bluetoothDevice.name ?: bluetoothDevice.address}")
//                    bluetoothGatt = bluetoothDevice.connectGatt(this, false, gattCallback)
//                } catch (exception: IllegalArgumentException) {
//                    Log.w(TAG, "Device not found with provided address")
//                    stopService(this)
//                }
            }
            ACTION_RUN -> {
                startForeground()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
//        TODO: REFACTOR
//        serviceScope.launch {
//            serviceRepository.updateIsKeyLinked(false)
//        }
        serviceScope.launch {
            serviceRepository.updateIsServiceRunning(false)
        }
        rssiPollerScope.cancel()
    }

//    TODO: REFACTOR
//    private val gattCallback = object : BluetoothGattCallback() {
//        override fun onReadRemoteRssi(gatt: BluetoothGatt?, readRssi: Int, status: Int) {
//            super.onReadRemoteRssi(gatt, readRssi, status)
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.d(TAG, "Device RSSI: $readRssi dBm")
//                serviceScope.launch {
//                    serviceRepository.updateRssi(readRssi)
//                }
//                serviceScope.launch {
//                    combine(
//                        serviceRepository.isServiceRunning,
//                        serviceRepository.rssi,
//                        settingsRepository.rssiThreshold
//                    ) {
//                        isServiceRunning, rssi, rssiThreshold -> listOf(isServiceRunning, rssi, rssiThreshold)
//                    }.collect { (isServiceRunning, rssi, rssiThreshold) ->
//                        Log.d(TAG, "RSSI: $rssi, THRESHOLD: $rssiThreshold")
//                        if(isServiceRunning as Boolean && (rssi as Int) < (rssiThreshold as Int)) {
//                            lockDevice()
//                        }
//                    }
//                }
//            }
//        }
//
//        @SuppressLint("MissingPermission")
//        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
//            super.onConnectionStateChange(gatt, status, newState)
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                Log.d(TAG, "Connected to Gatt Server")
//                serviceScope.launch {
//                    serviceRepository.updateIsKeyLinked(true)
//                }
//                rssiPollerScope.launch {
//                    while (true) {
//                        bluetoothGatt?.readRemoteRssi()
//                        delay(POLL_DELAY)
//                    }
//                }
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                Log.d(TAG, "Disconnected from GATT server")
//                serviceScope.launch {
//                    serviceRepository.updateIsKeyLinked(false)
//                }
//            }
//        }
//    }

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
                    this,
                    SERVICE_ID,
                    createNotification(),
                    0
                )
            }
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                Log.e(TAG, e.toString())
            }
        }
        serviceScope.launch {
            serviceRepository.updateIsServiceRunning(true)
        }
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, SuperSmartKeyService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val mainIntent = Intent(this, SuperSmartKeyActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.key)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainPendingIntent)
            .addAction(
                R.drawable.stop,
                getString(R.string.stop_service),
                stopPendingIntent
            )

        return notification.build()
    }

    private fun lockDevice() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, DeviceAdmin::class.java)
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        } else {
            Log.e(TAG, "Admin permission is not active")
        }
    }
}