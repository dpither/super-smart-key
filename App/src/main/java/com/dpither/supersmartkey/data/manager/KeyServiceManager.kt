package com.dpither.supersmartkey.data.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.dpither.supersmartkey.service.KeyService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "KEY_SERVICE_MANAGER"

@Singleton
class KeyServiceManager @Inject constructor(@ApplicationContext private val context: Context) {
    private lateinit var keyService: KeyService
    private var bound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "Service Connected")
            val binder = service as KeyService.KeyBinder
            keyService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            Log.d(TAG, "Service Disconnected")
            bound = false
        }
    }

    fun startKeyService() {
        if (!bound) {
            Log.d(TAG, "Binding to and starting key service")
            val intent = Intent(context, KeyService::class.java)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            context.startService(intent)
        }
    }

    fun stopKeyService() {
        if (bound) {
            Log.d(TAG, "Unbinding from and stopping key service")
            context.unbindService(connection)
            bound = false
            val intent = Intent(context, KeyService::class.java)
            context.stopService(intent)
        }
    }

    fun startLockService() {
        if (bound) {
            Log.d(TAG, "Starting lock service")
            keyService.startLockService()
        }
    }


    fun stopLockService() {
        if (bound) {
            Log.d(TAG, "Stopping lock service")
            keyService.stopLockService()
        }
    }

    fun bindToServiceIfRunning() {
        if (!bound && KeyService.isRunning) {
            Log.d(TAG, "Binding to service")
            val intent = Intent(context, KeyService::class.java)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        if (bound) {
            Log.d(TAG, "Unbinding from service")
            context.unbindService(connection)
            bound = false
        }
    }
}