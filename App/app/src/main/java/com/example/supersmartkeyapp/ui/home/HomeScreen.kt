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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.admin.DeviceAdmin
import com.example.supersmartkeyapp.data.model.Key
import com.example.supersmartkeyapp.ui.theme.AppTheme
import com.example.supersmartkeyapp.util.AvailableKeysDialog
import com.example.supersmartkeyapp.util.DEFAULT_ANIMATION_DURATION
import com.example.supersmartkeyapp.util.HomeTopAppBar
import com.example.supersmartkeyapp.util.PermissionRationaleDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.min

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

    PermissionRationaleDialog(
        visible = uiState.showBluetoothPermissionDialog,
        title = bluetoothPermissionTitle,
        rationale = bluetoothPermissionRationale,
        onConfirm = {
            bluetoothPermissionState.launchMultiplePermissionRequest()
            viewModel.closeBluetoothPermissionDialog()
        },
        onDismiss = viewModel::closeBluetoothPermissionDialog
    )

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

    PermissionRationaleDialog(
        visible = uiState.showDeviceAdminDialog,
        title = stringResource(R.string.device_admin),
        rationale = stringResource(R.string.device_admin_rationale),
        onConfirm = {
            deviceAdminLauncher.launch(deviceAdminIntent)
            viewModel.closeDeviceAdminDialog()
        },
        onDismiss = viewModel::closeDeviceAdminDialog
    )

    AvailableKeysDialog(
        visible = uiState.showAvailableKeysDialog,
        availableKeys = uiState.availableKeys,
        currentKey = uiState.key,
        selectedKey = uiState.selectedKey,
        onKeySelected = viewModel::selectKey,
        onDismiss = viewModel::closeAvailableKeysDialog,
        onConnect = viewModel::connectKey,
        onDisconnect = viewModel::disconnectKey
    )

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
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        HorizontalDivider()
        AnimatedVisibility(
            visible = key == null,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = LinearEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = LinearEasing
                )
            )
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_key_text),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        AnimatedVisibility(
            visible = key != null,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = LinearEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = LinearEasing
                )
            )
        ) {
            val rssi = key?.rssi ?: 0
            val progress = min(rssi / rssiThreshold.toFloat(), 1f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                KeyInfo(
                    key = key,
                    isKeyConnected = isKeyConnected
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    DistanceIndicator(
                        progress = progress, size = 240.dp
                    )
                    StartStopButton(
                        isServiceRunning = isServiceRunning,
                        size = 200.dp,
                        onStart = onStart,
                        onStop = onStop
                    )
                }

            }
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
    isServiceRunning: Boolean, size: Dp = 200.dp, onStart: () -> Unit, onStop: () -> Unit
) {
    ElevatedButton(
        onClick = { if (isServiceRunning) onStop() else onStart() },
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (isServiceRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        modifier = Modifier.size(size)
    ) {
        Text(
            text = if (isServiceRunning) stringResource(R.string.stop) else stringResource(R.string.start),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun DistanceIndicator(
    progress: Float,
    size: Dp = 240.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress animation"
    )
    val color =
        if (animatedProgress == 1f) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    Box(
        modifier = Modifier.size(size)
    ) {
//        Arc code by ChatGPT
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val diameter = size.toPx()
            val radius = (diameter - stroke) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val startAngle = 100f
            val sweepAngle = 340f * animatedProgress

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
        }
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.lock),
            contentDescription = stringResource(R.string.lock),
            tint = color,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(size / 10)
                .offset(y = 8.dp)
        )
    }
}

//TODO: Update
@Composable
private fun KeyInfo(
    key: Key?,
    isKeyConnected: Boolean,
) {
    val titleStyle = MaterialTheme.typography.titleMedium
    val textStyle = MaterialTheme.typography.bodyLarge
    val name = key?.name ?: " "
    val address = key?.address ?: " "
    val rssi = key?.rssi ?: 0
    val status = if (isKeyConnected) {
        stringResource(R.string.connected)
    } else {
        stringResource(R.string.disconnected)
    }
    Column {
        Text(text = stringResource(R.string.key_information), style = titleStyle)
        AnimatedContent(
            targetState = name,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION,
                        easing = LinearEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION,
                        easing = LinearEasing
                    )
                )
            },
            label = "name animation"
        ) { targetName ->
            Text(text = stringResource(R.string.name) + ": " + targetName, style = textStyle)
        }
        AnimatedContent(
            targetState = address,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION,
                        easing = LinearEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION,
                        easing = LinearEasing
                    )
                )
            },
            label = "address animation"
        ) { targetAddress ->
            Text(text = stringResource(R.string.address) + ": " + targetAddress, style = textStyle)
        }

        AnimatedContent(
            targetState = status,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION,
                        easing = LinearEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION,
                        easing = LinearEasing
                    )
                )
            },
            label = "status animation"
        ) { targetStatus ->
            Text(text = stringResource(R.string.status) + ": " + targetStatus, style = textStyle)
        }

        AnimatedVisibility(
            visible = isKeyConnected && key?.rssi != null,
            enter = slideInVertically(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = EaseIn
                )
            ) + expandVertically(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = EaseIn
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = LinearEasing
                )
            ),
            exit = slideOutVertically(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = EaseOut
                )
            ) + shrinkVertically(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = EaseOut
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = LinearEasing
                )
            )
        ) {
            Row {
                Text(
                    text = stringResource(R.string.rssi) + ": ",
                    style = textStyle
                )
                AnimatedContent(
                    targetState = rssi,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInVertically { height -> height } + fadeIn(
                                animationSpec = tween(
                                    durationMillis = DEFAULT_ANIMATION_DURATION,
                                    easing = LinearEasing
                                )
                            ) togetherWith slideOutVertically { height -> -height } + fadeOut(
                                animationSpec = tween(
                                    durationMillis = DEFAULT_ANIMATION_DURATION,
                                    easing = LinearEasing
                                )
                            )
                        } else {
                            slideInVertically { height -> -height } + fadeIn(
                                animationSpec = tween(
                                    durationMillis = DEFAULT_ANIMATION_DURATION,
                                    easing = LinearEasing
                                )
                            ) togetherWith slideOutVertically { height -> height } + fadeOut(
                                animationSpec = tween(
                                    durationMillis = DEFAULT_ANIMATION_DURATION,
                                    easing = LinearEasing
                                )
                            )
                        }
                    },
                    label = "rssi animation"
                ) { targetRssi ->
                    Text(text = "$targetRssi", style = textStyle)
                }
                Text(
                    text = " " + stringResource(R.string.rssi_threshold_units),
                    style = textStyle
                )
            }
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
                    name = "Smart Tag", address = "00:11:22:33:AA:BB", rssi = -62
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
                    name = "Smart Tag", address = "00:11:22:33:AA:BB", rssi = -100
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