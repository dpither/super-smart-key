package com.example.supersmartkeyapp.ui.home

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.admin.DeviceAdmin
import com.example.supersmartkeyapp.data.Key
import com.example.supersmartkeyapp.service.SuperSmartKeyService
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
            LinkKeyButton(
                isKeyLinked = uiState.key != null,
                onClick = {
                    SuperSmartKeyService.stopService(context)
                    viewModel.updateShowDialog(true)
                }
            )
        },
    ) { paddingValues ->

        HomeContent(
            availableKeys = uiState.availableKeys,
            key = uiState.key,
            isServiceRunning = uiState.isServiceRunning,
            onStart = {
                viewModel.updateIsServiceRunning(true)
//                requestDeviceAdmin(context = context, deviceAdminLauncher = deviceAdminLauncher)
//                SuperSmartKeyService.runService(context)
            },
            onStop = {
//                SuperSmartKeyService.stopService(context)
                viewModel.updateIsServiceRunning(false)
            },
            showDialog = uiState.showDialog,
//            TODO: updatedevice select
            onKeySelected = {
                viewModel.linkKey(it)
//                SuperSmartKeyService.startService(context, it.address)
            },
            closeDialog = { viewModel.updateShowDialog(false) },
            modifier = Modifier.padding(paddingValues)
        )
    }

}

@Composable
private fun HomeContent(
    key: Key?,
    isServiceRunning: Boolean,
    availableKeys: List<Key>,
    onKeySelected: (Key) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    showDialog: Boolean,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        AvailableKeysDialog(
            availableKeys = availableKeys,
            onDismiss = closeDialog,
            onKeySelected = onKeySelected
        )
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
                key = key,
                isServiceRunning = isServiceRunning,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            StartStopButton(
                isServiceRunning = isServiceRunning,
                isKeyLinked = (key != null),
                onStart = onStart,
                onStop = onStop,
            )
        }
    }
}

@Composable
private fun LinkKeyButton(
    isKeyLinked: Boolean,
    onClick: () -> Unit
) {
    RequestPermissions()
    LargeFloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            imageVector =
            if (isKeyLinked) {
                ImageVector.vectorResource(R.drawable.swap)
            } else {
                ImageVector.vectorResource(R.drawable.link)
            },
            contentDescription =
            if (isKeyLinked) {
                stringResource(R.string.change_key)
            } else {
                stringResource(R.string.link_key)
            },
            modifier =
            if (isKeyLinked) {
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
    key: Key?,
    isServiceRunning: Boolean,
) {
    val titleStyle = MaterialTheme.typography.titleSmall
    val textStyle = MaterialTheme.typography.bodyLarge
    val isKeyLinked = key != null
    Column {
        Text(
            text = "Target Key:",
            style = titleStyle
        )
        if (key != null) {
            Text(
                text = key.name,
                style = textStyle
            )
        } else {
            Text("")
        }
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
        if (key != null) {
            Text(
                text = "${key.rssi} dBm",
                style = textStyle
            )
        } else {
            Text("")
        }
    }
}

@Composable
private fun AvailableKeysDialog(
    availableKeys: List<Key>,
    onDismiss: () -> Unit,
    onKeySelected: (Key) -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.key_dialog_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.key_dialog_text),
                    )
                }
                Column {
                    HorizontalDivider()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .verticalScroll(scrollState)
                    ) {
//                    TODO:
                        if (availableKeys.isEmpty()) {
                            Text("EMPTYYYYYYY")
                        } else {
                            availableKeys.forEachIndexed { index, key ->
                                KeyRow(
                                    name = key.name,
                                    address = key.device.address,
                                    onClick = { onKeySelected(key) }
                                )
                                if (index != availableKeys.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }
                TextButton(
                    onClick = { onDismiss() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(),

                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
private fun KeyRow(
    name: String,
    address: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Address: $address",
                style = MaterialTheme.typography.bodySmall,
            )
        }
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
            topBar = { HomeTopAppBar(onSettings = {}) },
            floatingActionButton = {
                LinkKeyButton(isKeyLinked = false, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(
                key = null,
                availableKeys = emptyList(),
                isServiceRunning = false,
                onStart = {},
                onStop = {},
                closeDialog = {},
                onKeySelected = {},
                showDialog = false,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Preview
@Composable
fun AvailableKeysDialogPreview() {
    AppTheme {
        AvailableKeysDialog(
            availableKeys = emptyList(),
            onDismiss = {},
            onKeySelected = {}
        )
    }
}

@Preview
@Composable
fun KeyRowPreview() {
    AppTheme {
        KeyRow(
            name = "Smart Tag2",
            address = "00:11:22:AA:BB:CC",
            onClick = {}
        )
    }
}