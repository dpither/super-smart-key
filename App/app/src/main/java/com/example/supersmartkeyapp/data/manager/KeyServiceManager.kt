package com.example.supersmartkeyapp.data.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.supersmartkeyapp.service.KeyService
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
            val binder = service as KeyService.KeyBinder
            keyService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            bound = false
        }
    }

    fun startKeyService() {
        val intent = Intent(context, KeyService::class.java)
        if (!bound) {
            Log.d(TAG, "Binding Service")
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        context.startService(intent)
    }

    fun runKeyService() {
        if (bound) {
            keyService.runService()
        }
    }


    fun pauseKeyService() {
        if (bound) {
            keyService.pauseService()
        }
    }

    fun stopKeyService() {
        if (bound) {
            Log.d(TAG, "Unbinding Service")
            context.unbindService(connection)
            bound = false
        }
        val intent = Intent(context, KeyService::class.java)
        context.stopService(intent)
    }
}