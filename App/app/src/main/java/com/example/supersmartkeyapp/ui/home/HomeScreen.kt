package com.example.supersmartkeyapp.ui.home

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.admin.DeviceAdmin
import com.example.supersmartkeyapp.service.SuperSmartKeyService
import com.example.supersmartkeyapp.ui.BluetoothDialog
import com.example.supersmartkeyapp.ui.theme.AppTheme
import com.example.supersmartkeyapp.util.HomeTopAppBar
import com.example.supersmartkeyapp.util.RequestPermissions

@Composable
fun HomeScreen(
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Admin enabled
            Log.d("HOME", "Admin enabled")
        } else {
            // Admin not enabled
            Log.d("HOME", "Admin not enabled")
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { HomeTopAppBar(onSettings) },
        floatingActionButton = {
            LinkButton(
                isLinked = uiState.isKeyLinked,
                onClick = {
                    SuperSmartKeyService.stopService(context)
                    viewModel.updateShowDialog(true)
                }
            )
        },
    ) { paddingValues ->

        HomeContent(
            deviceAddress = uiState.deviceAddress,
            deviceName = uiState.deviceName,
            rssi = uiState.rssi,
            isServiceRunning = uiState.isServiceRunning,
            isKeyLinked = uiState.isKeyLinked,
            onStart = {
                requestDeviceAdmin(context = context, deviceAdminLauncher = deviceAdminLauncher)
                SuperSmartKeyService.runService(context)
            },
            onStop = {
                SuperSmartKeyService.stopService(context)
            },
            showDialog = uiState.showDialog,
            onDeviceSelected = {
                viewModel.updateDevice(it)
                SuperSmartKeyService.startService(context, it.address)
            },
            closeDialog = { viewModel.updateShowDialog(false) },
            modifier = Modifier.padding(paddingValues)
        )
    }

}

@Composable
private fun HomeContent(
    deviceAddress: String,
    deviceName: String,
    rssi: Int,
    isServiceRunning: Boolean,
    isKeyLinked: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    closeDialog: () -> Unit,
    showDialog: Boolean,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        BluetoothDialog(onDismiss = closeDialog, onDeviceSelected = onDeviceSelected)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            //                Top of Large FAB is 112
            .padding(bottom = 144.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            StatusText(
                deviceAddress = deviceAddress,
                deviceName = deviceName,
                rssi = rssi,
                isServiceRunning = isServiceRunning,
                isKeyLinked = isKeyLinked
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            StartStopButton(
                isServiceRunning = isServiceRunning,
                isKeyLinked = isKeyLinked,
                onStart = onStart,
                onStop = onStop,
            )
        }
    }
}

@Composable
private fun LinkButton(
    isLinked: Boolean,
    onClick: () -> Unit
) {
    RequestPermissions()
    LargeFloatingActionButton(
        onClick =
        onClick
    ) {
        Icon(
            imageVector =
            if (isLinked) {
                ImageVector.vectorResource(R.drawable.swap)
            } else {
                ImageVector.vectorResource(R.drawable.link)
            },
            contentDescription =
            if (isLinked) {
                stringResource(R.string.change_key)
            } else {
                stringResource(R.string.link_key)
            },
            modifier =
            if (isLinked) {
                Modifier.rotate(0f)
            } else {
                Modifier.rotate(45f)
            },
        )
    }
}

@Composable
private fun StartStopButton(
    isServiceRunning: Boolean,
    isKeyLinked: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { if (isServiceRunning) onStop() else onStart() },
        colors = ButtonDefaults.buttonColors(containerColor = if (isServiceRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
        enabled = isKeyLinked,
        modifier = modifier.width(100.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(text = if (isServiceRunning) "Stop" else "Start")
        }
    }
}

@Composable
private fun StatusText(
    deviceAddress: String,
    deviceName: String,
    rssi: Int,
    isServiceRunning: Boolean,
    isKeyLinked: Boolean,
) {
    val titleStyle = MaterialTheme.typography.titleSmall
    val textStyle = MaterialTheme.typography.bodyLarge
    Column {
        Text(
            text = "Target Key:",
            style = titleStyle
        )
        Text(
            text =
            if (isKeyLinked) {
                deviceName.ifEmpty {
                    deviceAddress
                }
            } else {
                ""
            },
            style = textStyle
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Status:",
            style = titleStyle
        )
        Text(
            text = "Key Linked: $isKeyLinked",
            style = textStyle
        )
        Text(
            text = "Service Running: $isServiceRunning",
            style = textStyle
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "RSSI:",
            style = titleStyle
        )
        Text(
            text = if (isKeyLinked) {
                "$rssi dBm"
            } else {
                ""
            },
            style = textStyle
        )

    }
}

private fun requestDeviceAdmin(
    context: Context,
    deviceAdminLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val componentName = ComponentName(context, DeviceAdmin::class.java)
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Activate to enable locking"
        )
    }
    deviceAdminLauncher.launch(intent)
}

@Preview
@Composable
fun HomeContentPreview() {
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                LinkButton(isLinked = false, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(
                isServiceRunning = false,
                isKeyLinked = true,
                onStart = {},
                onStop = {},
                closeDialog = {},
                deviceAddress = "",
                deviceName = "ESP 32",
                rssi = 0,
                onDeviceSelected = {},
                showDialog = false,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}