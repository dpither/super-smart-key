package com.example.supersmartkeyapp.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.data.model.Key
import com.example.supersmartkeyapp.ui.theme.AppTheme

@Composable
fun PermissionRationaleDialog(
    title: String,
    rationale: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 16.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                HorizontalDivider()
                Text(
                    text = rationale,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(24.dp)
                )
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 24.dp)
                        .padding(horizontal = 24.dp)
                        .align(Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    TextButton(
                        onClick = onConfirm
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableKeysDialog(
    availableKeys: List<Key>,
    currentKey: Key?,
    onKeySelected: (Key) -> Unit,
    onDismiss: () -> Unit,
    onDisconnect: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
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

@Preview
@Composable
private fun PermissionRationaleDialogPreview() {
    AppTheme {
        PermissionRationaleDialog(title = stringResource(R.string.bluetooth_permissions),
            rationale = stringResource(R.string.bluetooth_denied_rationale),
            onConfirm = {},
            onDismiss = {})
    }
}

@Preview
@Composable
private fun AvailableKeysDialogPreview() {
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
private fun KeyRowPreview() {
    AppTheme {
        KeyRow(name = "Smart Tag2", address = "00:11:22:AA:BB:CC", selected = true, onClick = {})
    }
}