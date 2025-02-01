package com.example.supersmartkeyapp.ui.home

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.supersmartkeyapp.data.model.Key
import com.example.supersmartkeyapp.ui.theme.AppTheme
import com.example.supersmartkeyapp.util.AvailableKeysDialog
import com.example.supersmartkeyapp.util.HomeTopAppBar
import com.example.supersmartkeyapp.util.PermissionRationaleDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private const val TAG = "HOME_SCREEN"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val bluetoothPermissionState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    )

    val bluetoothPermissionTitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        stringResource(R.string.bluetooth_permissions)
    } else {
        stringResource(R.string.location_permissions)
    }

    val bluetoothPermissionRationale = if (bluetoothPermissionState.shouldShowRationale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            stringResource(R.string.bluetooth_rationale)
        } else {
            stringResource(R.string.location_rationale)
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            stringResource(R.string.bluetooth_denied_rationale)
        } else {
            stringResource(R.string.location_denied_rationale)

        }
    }

    if (uiState.showBluetoothPermissionDialog) {
        PermissionRationaleDialog(
            title = bluetoothPermissionTitle,
            rationale = bluetoothPermissionRationale,
            onConfirm = {
                bluetoothPermissionState.launchMultiplePermissionRequest()
                viewModel.closeBluetoothPermissionDialog()
            },
            onDismiss = viewModel::closeBluetoothPermissionDialog
        )
    }

    val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    val deviceAdminIntent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
        putExtra(
            DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(context, DeviceAdmin::class.java)
        )
        putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            stringResource(R.string.device_admin_rationale)
        )
    }

    val deviceAdminLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    if (uiState.showDeviceAdminDialog) {
        PermissionRationaleDialog(
            title = stringResource(R.string.device_admin),
            rationale = stringResource(R.string.device_admin_rationale),
            onConfirm = {
                deviceAdminLauncher.launch(deviceAdminIntent)
                viewModel.closeDeviceAdminDialog()
            },
            onDismiss = viewModel::closeDeviceAdminDialog
        )
    }

    if (uiState.showAvailableKeysDialog) {
        AvailableKeysDialog(
            availableKeys = uiState.availableKeys,
            currentKey = uiState.key,
            selectedKey = uiState.selectedKey,
            onKeySelected = viewModel::selectKey,
            onDismiss = viewModel::closeAvailableKeysDialog,
            onConnect = viewModel::connectKey,
            onDisconnect = viewModel::disconnectKey
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { HomeTopAppBar(onSettings) },
        floatingActionButton = {
            LinkKeyButton(isKeyConnected = uiState.key != null, onClick = {
                if (bluetoothPermissionState.allPermissionsGranted) {
                    viewModel.openAvailableKeysDialog()
                } else {
                    viewModel.openBluetoothPermissionDialog()
                }
            })
        },
    ) { paddingValues ->

        HomeContent(
            key = uiState.key, isServiceRunning = uiState.isServiceRunning,
            onStart = {
                if (devicePolicyManager.isAdminActive(
                        ComponentName(
                            context, DeviceAdmin::class.java
                        )
                    )
                ) {
                    viewModel.runKeyService()
                } else {
                    viewModel.openDeviceAdminDialog()
                }
            }, onStop = viewModel::pauseKeyService, modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun HomeContent(
    key: Key?,
    isServiceRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            //                Top of Large FAB is 112
            .padding(bottom = 144.dp)
            .padding(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            StatusText(
                key = key,
                isServiceRunning = isServiceRunning,
            )
        }
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            StartStopButton(
                isServiceRunning = isServiceRunning,
                isKeyConnected = (key != null),
                onStart = onStart,
                onStop = onStop,
            )
        }
    }
}

@Composable
private fun LinkKeyButton(
    isKeyConnected: Boolean, onClick: () -> Unit
) {
    LargeFloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = if (isKeyConnected) {
                ImageVector.vectorResource(R.drawable.swap)
            } else {
                ImageVector.vectorResource(R.drawable.link)
            },
            contentDescription = if (isKeyConnected) {
                stringResource(R.string.change_key)
            } else {
                stringResource(R.string.link_key)
            },
            modifier = if (isKeyConnected) {
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
    isKeyConnected: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { if (isServiceRunning) onStop() else onStart() },
        colors = ButtonDefaults.buttonColors(containerColor = if (isServiceRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
        enabled = isKeyConnected,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = if (isServiceRunning) stringResource(R.string.stop) else stringResource(R.string.start))
    }
}

//TODO: Update
@Composable
private fun StatusText(
    key: Key?,
    isServiceRunning: Boolean,
) {
    val titleStyle = MaterialTheme.typography.titleSmall
    val textStyle = MaterialTheme.typography.bodyLarge
    val isKeyConnected = key != null
    Column {
        Text(
            text = "Target Key:", style = titleStyle
        )
        if (key != null) {
            Text(
                text = key.name, style = textStyle
            )
        } else {
            Text("")
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Status:", style = titleStyle
        )
        Text(
            text = "Key Linked: $isKeyConnected", style = textStyle
        )
        Text(
            text = "Service Running: $isServiceRunning", style = textStyle
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "RSSI:", style = titleStyle
        )
        if (key != null) {
            Text(
                text = "${key.rssi} dBm", style = textStyle
            )
        } else {
            Text("")
        }
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { HomeTopAppBar(onSettings = {}) },
            floatingActionButton = {
                LinkKeyButton(isKeyConnected = false, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(
                key = null,
                isServiceRunning = false,
                onStart = {},
                onStop = {},
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}