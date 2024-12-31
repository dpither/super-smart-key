package com.example.supersmartkeyapp.util

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RequestPermissions() {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
//            Manifest.permission.FOREGROUND_SERVICE,
//            Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
        )
    } else {
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            permissionsMap.forEach { (permission, isGranted) ->
                if (isGranted) {
                    Log.d("RequestPermissions", "$permission granted")
                } else {
                    Log.d("RequestPermissions", "$permission denied")
                }
            }
        }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions.toTypedArray())
    }
}