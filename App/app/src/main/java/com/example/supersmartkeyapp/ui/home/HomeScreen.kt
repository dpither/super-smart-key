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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.admin.DeviceAdmin
import com.example.supersmartkeyapp.data.model.Key
import com.example.supersmartkeyapp.ui.theme.AppTheme
import com.example.supersmartkeyapp.util.HomeTopAppBar
import com.example.supersmartkeyapp.util.RequestPermissions

private const val TAG = "HOME_SCREEN"

@Composable
fun HomeScreen(
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//    TODO: Move device admin perms
    val context = LocalContext.current

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Admin enabled
            Log.d(TAG, "Admin enabled")
        } else {
            // Admin not enabled
            Log.d(TAG, "Admin not enabled")
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { HomeTopAppBar(onSettings) },
        floatingActionButton = {
            LinkKeyButton(
                isKeyConnected = uiState.key != null, onClick = viewModel::openDialog
            )
        },
    ) { paddingValues ->

        HomeContent(
            key = uiState.key,
            isServiceRunning = uiState.isServiceRunning,
            availableKeys = uiState.availableKeys,
            onKeySelected = viewModel::linkKey,
            onStart = viewModel::runKeyService,
            onStop = viewModel::pauseKeyService,
            onDisconnect = viewModel::unlinkKey,
            showDialog = uiState.showDialog,
            closeDialog = viewModel::closeDialog,
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
    onDisconnect: () -> Unit,
    showDialog: Boolean,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        AvailableKeysDialog(
            availableKeys = availableKeys,
            currentKey = key,
            onKeySelected = onKeySelected,
            onDismiss = closeDialog,
            onDisconnect = onDisconnect
        )
    }
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
//    TODO: Refactor permissions
    RequestPermissions()
    LargeFloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = if (isKeyConnected) {
                ImageVector.vectorResource(R.drawable.swap)
            } else {
                ImageVector.vectorResource(R.drawable.link)
            },
//           TODO: Add other content descriptions to other components?
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

@Composable
private fun AvailableKeysDialog(
    availableKeys: List<Key>,
    currentKey: Key?,
    onKeySelected: (Key) -> Unit,
    onDismiss: () -> Unit,
    onDisconnect: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 16.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.key_dialog_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.key_dialog_text),
                    )
                }
                HorizontalDivider()
                Column {
                    Column(
                        verticalArrangement = if (availableKeys.isEmpty()) {
                            Arrangement.Center
                        } else {
                            Arrangement.Top
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .verticalScroll(scrollState)
                    ) {
                        availableKeys.forEach { key ->
                            KeyRow(name = key.name,
                                address = key.device.address,
                                selected = key.device.address == currentKey?.device?.address,
                                onClick = {
                                    onKeySelected(key)
                                    onDismiss()
                                })
                        }
                        if (availableKeys.isEmpty()) {
                            Text(
                                text = stringResource(R.string.empty_key_dialog_title),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = stringResource(R.string.empty_key_dialog_text),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
                HorizontalDivider()
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 24.dp)
                        .padding(horizontal = 24.dp)
                        .align(Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    TextButton(enabled = currentKey != null, onClick = {
                        onDisconnect()
                        onDismiss()
                    }) {
                        Text(text = stringResource(R.string.disconnect))
                    }
                }

            }
        }
    }
}

@Composable
private fun KeyRow(
    name: String,
    address: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.address) + ": $address",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun requestDeviceAdmin(
    context: Context, deviceAdminLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val componentName = ComponentName(context, DeviceAdmin::class.java)
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Activate to enable locking"
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
                LinkKeyButton(isKeyConnected = false, onClick = {})
            },
        ) { paddingValues ->
            HomeContent(key = null,
                availableKeys = emptyList(),
                isServiceRunning = false,
                onStart = {},
                onStop = {},
                closeDialog = {},
                onKeySelected = {},
                showDialog = false,
                onDisconnect = {},
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Preview
@Composable
fun AvailableKeysDialogPreview() {
    AppTheme {
        AvailableKeysDialog(availableKeys = emptyList(),
            currentKey = null,
            onDismiss = {},
            onKeySelected = {},
            onDisconnect = {})
    }
}

@Preview
@Composable
fun KeyRowPreview() {
    AppTheme {
        KeyRow(name = "Smart Tag2", address = "00:11:22:AA:BB:CC", selected = true, onClick = {})
    }
}