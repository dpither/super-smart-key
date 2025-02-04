package com.example.supersmartkeyapp.ui.home

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
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
//            TODO: utilize is iskeyconnected
        HomeContent(
            key = uiState.key,
            rssiThreshold = uiState.rssiThreshold,
            isKeyConnected = true,
            isServiceRunning = uiState.isServiceRunning,
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
            },
            onStop = viewModel::pauseKeyService,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun HomeContent(
    key: Key?,
    rssiThreshold: Int,
    isKeyConnected: Boolean,
    isServiceRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
//            Large FAB is 96, padding is 16 (96 + 16 +16 = 128)
            .padding(bottom = 128.dp)
    ) {
        HorizontalDivider()
        if (key == null) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = stringResource(R.string.no_key_text),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            Column(
//                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                StatusText(
                    key = key,
                    rssiThreshold = rssiThreshold,
                    isServiceRunning = isServiceRunning,
                    isKeyConnected = isKeyConnected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                val rssi = key.rssi ?: 0
                Box(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp)) {
                    DistanceIndicator(
                        progress = rssi/rssiThreshold.toFloat(),
                        isServiceRunning = isServiceRunning,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    StartStopButton(
                        isServiceRunning = isServiceRunning,
                        onStart = onStart,
                        onStop = onStop,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(240.dp)
                    )
                }

            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun LinkKeyButton(
    isKeyConnected: Boolean, onClick: () -> Unit
) {
    LargeFloatingActionButton(
        onClick = onClick
    ) {
        if (isKeyConnected) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.swap),
                contentDescription = stringResource(R.string.change_key),
                modifier = Modifier.rotate(0f),
            )
        } else {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.link),
                contentDescription = stringResource(R.string.link_key),
                modifier = Modifier.rotate(45f),
            )
        }
    }
}

@Composable
private fun StartStopButton(
    isServiceRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { if (isServiceRunning) onStop() else onStart() },
        colors = ButtonDefaults.buttonColors(containerColor = if (isServiceRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
    ) {
        Text(text = if (isServiceRunning) stringResource(R.string.stop) else stringResource(R.string.start))
    }
}

@Composable
private fun DistanceIndicator(
    progress: Float, isServiceRunning: Boolean, modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "Distance Progress Animation"
    )
    Box(
        modifier = modifier
            .size(240.dp)
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .rotate(180f)
                .fillMaxSize()
                .align(Alignment.Center),
            color = if (isServiceRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            trackColor = Color.Transparent,
            strokeWidth = 8.dp,
        )
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.lock),
            contentDescription = stringResource(R.string.lock),
            tint = if (isServiceRunning && animatedProgress >= 1.0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(24.dp)
                .offset(y = 36.dp)
        )
    }
}

//TODO: Update
@Composable
private fun StatusText(
    key: Key,
    rssiThreshold: Int,
    isKeyConnected: Boolean,
    isServiceRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val titleStyle = MaterialTheme.typography.titleMedium
    val textStyle = MaterialTheme.typography.bodyLarge
    Column(modifier = modifier) {
        Text(text = stringResource(R.string.key_information), style = titleStyle)
        Text(text = stringResource(R.string.name) + ": " + key.name, style = textStyle)
        Text(text = stringResource(R.string.address) + ": " + key.address, style = textStyle)
        if (isKeyConnected) {
            Text(
                text = stringResource(R.string.status) + ": " + stringResource(R.string.connected),
                style = textStyle
            )
            Text(
                text = stringResource(R.string.rssi) + ": " + key.rssi + " " + stringResource(R.string.rssi_threshold_units),
                style = textStyle
            )
        } else {
            Text(
                text = stringResource(R.string.status) + ": " + stringResource(R.string.disconnected),
                style = textStyle
            )
            Text(
                text = " ",
                style = textStyle
            )
        }
    }
}

@Preview
@Composable
private fun HomeContentNoKeyPreview() {
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
                rssiThreshold = -70,
                isKeyConnected = false,
                isServiceRunning = false,
                onStart = {},
                onStop = {},
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Preview
@Composable
private fun HomeContentKeyPreview() {
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { HomeTopAppBar(onSettings = {}) },
            floatingActionButton = {
                LinkKeyButton(isKeyConnected = true, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(
                key = Key(
                    name = "Smart Tag", address = "00:11:22:33:AA:BB", rssi = -75
                ),
                rssiThreshold = -100,
                isKeyConnected = true,
                isServiceRunning = false,
                onStart = {},
                onStop = {},
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeContentKeyDisconnectedPreview() {
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { HomeTopAppBar(onSettings = {}) },
            floatingActionButton = {
                LinkKeyButton(isKeyConnected = false, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(
                key = Key(
                    name = "Smart Tag", address = "00:11:22:33:AA:BB", rssi = -99
                ),
                rssiThreshold = -100,
                isKeyConnected = false,
                isServiceRunning = false,
                onStart = {},
                onStop = {},
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}