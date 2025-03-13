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
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.supersmartkeyapp.util.MAX_RSSI
import com.example.supersmartkeyapp.util.PermissionRationaleDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.min

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

//    Bluetooth permissions
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

//    Dialog for Bluetooth permissions
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

//    Device admin permissions
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

//    Dialog for device admin
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
            LinkKeyButton(isKeyLinked = uiState.key != null, onClick = {
                if (bluetoothPermissionState.allPermissionsGranted) {
                    viewModel.openAvailableKeysDialog()
                } else {
                    viewModel.openBluetoothPermissionDialog()
                }
            })
        },
    ) { paddingValues ->
        HomeContent(
            key = uiState.key,
            rssiThreshold = uiState.rssiThreshold,
            isServiceRunning = uiState.isLockServiceRunning,
            onStart = {
                if (devicePolicyManager.isAdminActive(
                        ComponentName(context, DeviceAdmin::class.java)
                    )
                ) {
                    viewModel.startLockService()
                } else {
                    viewModel.openDeviceAdminDialog()
                }
            },
            onStop = viewModel::stopLockService,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun HomeContent(
    key: Key?,
    rssiThreshold: Int,
    isServiceRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val smallIconDp = with(LocalDensity.current) {
        MaterialTheme.typography.bodyLarge.lineHeight.toDp() * 4
    }
    val largeIconDp = smallIconDp * 2

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalDivider()
//        Icon animations
        val targetState = key == null
        val transition = updateTransition(targetState = targetState, label = "icon transition")

        val animatedRotation by transition.animateFloat(
            transitionSpec = {
                tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = FastOutSlowInEasing
                )
            }, label = "rotation animation"
        ) { state ->
            if (state) 0f else -90f
        }

        val animatedSize by transition.animateDp(
            transitionSpec = {
                tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = FastOutSlowInEasing
                )
            }, label = "size animation"
        ) { state ->
            if (state) largeIconDp else smallIconDp
        }

        val targetAlignment = if (targetState) Alignment.Center else Alignment.TopStart
        val biased = targetAlignment as BiasAlignment
        val animatedHorizontalAlignment by animateFloatAsState(
            targetValue = biased.horizontalBias, animationSpec = tween(
                durationMillis = DEFAULT_ANIMATION_DURATION, easing = FastOutSlowInEasing
            ), label = "horizontal alignment animation"
        )
        val animatedVerticalAlignment by animateFloatAsState(
            targetValue = biased.verticalBias, animationSpec = tween(
                durationMillis = DEFAULT_ANIMATION_DURATION, easing = FastOutSlowInEasing
            ), label = "vertical alignment animation"
        )
        val animatedAlignment =
            BiasAlignment(animatedHorizontalAlignment, animatedVerticalAlignment)

        Box(
            contentAlignment = animatedAlignment, modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.icon),
                contentDescription = stringResource(R.string.icon),
                modifier = Modifier
                    .rotate(animatedRotation)
                    .size(animatedSize)
            )
        }

//        Content when no key connected
        AnimatedVisibility(
            visible = key == null, enter = fadeIn(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                )
            ), exit = fadeOut(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 128.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = stringResource(R.string.no_key_text),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 128.dp)
                    .padding(horizontal = 16.dp), contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = stringResource(R.string.call_to_action),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

        }

//        Content when key connected
        AnimatedVisibility(
            visible = key != null, enter = fadeIn(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                )
            ), exit = fadeOut(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                )
            )
        ) {
            val progress =
                if (key?.rssi != null) min(key.rssi / rssiThreshold.toFloat(), 1f) else 0f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row {
                    Spacer(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(smallIconDp)
                    )
                    KeyInfo(key = key)
                }
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.align(Alignment.Center)
                ) {
                    DistanceIndicator(progress = progress)
                    StartStopButton(
                        isServiceRunning = isServiceRunning, onStart = onStart, onStop = onStop
                    )
                }

            }
        }
    }
}

@Composable
private fun LinkKeyButton(isKeyLinked: Boolean, onClick: () -> Unit) {
    LargeFloatingActionButton(
        onClick = onClick
    ) {
        if (isKeyLinked) {
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
private fun StartStopButton(isServiceRunning: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    ElevatedButton(
        onClick = { if (isServiceRunning) onStop() else onStart() },
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (isServiceRunning) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }, contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        modifier = Modifier.size(200.dp)
    ) {
        Text(
            text = if (isServiceRunning) stringResource(R.string.stop) else stringResource(R.string.start),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun DistanceIndicator(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress animation"
    )
    val color = if (animatedProgress == 1f) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val size = 240.dp

    Box(modifier = Modifier.size(size)) {
//        Base arc code by ChatGPT, modified to fit vision
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

@Composable
private fun KeyInfo(key: Key?) {
    val name = key?.name ?: " "
    val address = key?.address ?: " "
    val rssi = key?.rssi ?: 0
    val connected = key?.connected == true && key.rssi != null && key.rssi != MAX_RSSI
    val status = if (connected) {
        stringResource(R.string.connected)
    } else {
        stringResource(R.string.disconnected)
    }

    val titleStyle = MaterialTheme.typography.titleMedium
    val textStyle = MaterialTheme.typography.bodyLarge

    Column {
//        Name
        AnimatedContent(
            targetState = name, transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                    )
                )
            }, label = "name animation"
        ) { targetName ->
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = stringResource(R.string.name) + ": ", style = titleStyle)
                Text(
                    text = targetName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle
                )
            }
        }
//        Address
        AnimatedContent(
            targetState = address, transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                    )
                )
            }, label = "address animation"
        ) { targetAddress ->
            Row {
                Text(text = stringResource(R.string.address) + ": ", style = titleStyle)
                Text(text = targetAddress, style = textStyle)
            }
        }
//        Status
        AnimatedContent(
            targetState = status, transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                    )
                )
            }, label = "status animation"
        ) { targetStatus ->
            Row {
                Text(text = stringResource(R.string.status) + ": ", style = titleStyle)
                Text(text = targetStatus, style = textStyle)
            }
        }
//        RSSI
        AnimatedVisibility(
            visible = connected, enter = slideInVertically(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = EaseIn
                )
            ) + expandVertically(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = EaseIn
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                )
            ), exit = slideOutVertically(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = EaseOut
                )
            ) + shrinkVertically(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = EaseOut
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                )
            )
        ) {
            Row {
                Text(text = stringResource(R.string.rssi) + ": ", style = titleStyle)
//                RSSI Ticking
                AnimatedContent(
                    targetState = rssi, transitionSpec = {
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
                    }, label = "rssi animation"
                ) { targetRssi ->
                    Text(text = "$targetRssi", style = textStyle)
                }
                Text(text = " " + stringResource(R.string.rssi_threshold_units), style = textStyle)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.PIXEL)
@Composable
private fun HomeContentNoKeyPreview() {
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { HomeTopAppBar(onSettings = {}) },
            floatingActionButton = {
                LinkKeyButton(isKeyLinked = false, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(
                key = null,
                rssiThreshold = -70,
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
                LinkKeyButton(isKeyLinked = true, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(
                key = Key(
                    name = "Smart Tag Smart Tag Smart Tag Smart Tag",
                    address = "00:11:22:33:AA:BB",
                    lastSeen = null,
                    rssi = -62,
                    connected = true
                ),
                rssiThreshold = -100,
                isServiceRunning = false,
                onStart = {},
                onStop = {},
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.PIXEL)
@Composable
private fun HomeContentKeyDisconnectedPreview() {
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { HomeTopAppBar(onSettings = {}) },
            floatingActionButton = {
                LinkKeyButton(isKeyLinked = false, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(
                key = Key(
                    name = "Smart Tag",
                    address = "00:11:22:33:AA:BB",
                    lastSeen = null,
                    rssi = -100,
                    connected = false
                ),
                rssiThreshold = -100,
                isServiceRunning = false,
                onStart = {},
                onStop = {},
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.PIXEL_3A)
@Composable
private fun HomeContentKeyDisconnectedPreview2() {
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { HomeTopAppBar(onSettings = {}) },
            floatingActionButton = {
                LinkKeyButton(isKeyLinked = false, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(
                key = Key(
                    name = "Smart Tag Smart Tag Smart Tag",
                    address = "00:11:22:33:AA:BB",
                    lastSeen = null,
                    rssi = -100,
                    connected = false
                ),
                rssiThreshold = -100,
                isServiceRunning = false,
                onStart = {},
                onStop = {},
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}