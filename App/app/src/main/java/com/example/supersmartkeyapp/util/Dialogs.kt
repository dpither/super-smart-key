package com.example.supersmartkeyapp.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.data.model.Key
import com.example.supersmartkeyapp.ui.theme.AppTheme

@Composable
fun PermissionRationaleDialog(
    visible: Boolean,
    title: String,
    rationale: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    CustomDialog(showDialog = visible, onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column {
//                Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
//                Rationale
                Text(
                    text = rationale,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
                HorizontalDivider()
                Row(modifier = Modifier.padding(16.dp)) {
//                    Cancel Button
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
//                    OK Button
                    TextButton(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableKeysDialog(
    visible: Boolean,
    availableKeys: List<Key>,
    currentKey: Key?,
    selectedKey: Key?,
    onKeySelected: (Key) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    CustomDialog(showDialog = visible, onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column {
//                Title
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.key_dialog_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(text = stringResource(R.string.key_dialog_text))
                }
                HorizontalDivider()
                Column {
//                    List
                    Column(
                        verticalArrangement = if (availableKeys.isEmpty()) Arrangement.Center
                        else Arrangement.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .verticalScroll(scrollState)
                    ) {
                        availableKeys.forEach { key ->
                            KeyRow(name = key.name,
                                address = key.address,
                                selected = key.address == selectedKey?.address,
                                onClick = { onKeySelected(key) })
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
                Row(modifier = Modifier.padding(16.dp)) {
//                    Cancel Button
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    if (selectedKey != null && selectedKey.address == currentKey?.address) {
//                        If selected key is the current key Disconnect Button
                        TextButton(onClick = {
                            onDisconnect()
                            onDismiss()
                        }, modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.disconnect))
                        }
                    } else {
//                        Else Connected Button, enabled only if a key selected
                        TextButton(enabled = selectedKey != null, onClick = {
                            onConnect()
                            onDismiss()
                        }, modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.connect))
                        }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.address) + ": $address",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CustomDialog(
    showDialog: Boolean, onDismissRequest: () -> Unit, content: @Composable () -> Unit
) {
    var showAnimatedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showDialog) {
        if (showDialog) showAnimatedDialog = true
    }

    if (showAnimatedDialog) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                var animateIn by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { animateIn = true }

//                Dismiss when tapping outside dialog
                Box(modifier = Modifier
                    .pointerInput(Unit) { detectTapGestures { onDismissRequest() } }
                    .fillMaxSize())

//                Content Animation
                AnimatedVisibility(
                    visible = animateIn && showDialog, enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                        )
                    ) + slideInVertically(animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION, easing = EaseIn
                    ), initialOffsetY = { it }), exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = DEFAULT_ANIMATION_DURATION, easing = LinearEasing
                        )
                    ) + slideOutVertically(animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION, easing = EaseOut
                    ), targetOffsetY = { it }), label = "dialog animation"
                ) {
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .pointerInput(Unit) { detectTapGestures { } }
                            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                            .width(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)) {
                        content()
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            showAnimatedDialog = false
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PermissionRationaleDialogPreview() {
    AppTheme {
        Surface {
            PermissionRationaleDialog(visible = true,
                title = stringResource(R.string.bluetooth_permissions),
                rationale = stringResource(R.string.bluetooth_denied_rationale),
                onConfirm = {},
                onDismiss = {})
        }
    }
}

@Preview
@Composable
private fun AvailableKeysDialogPreview() {
    AppTheme {
        AvailableKeysDialog(availableKeys = emptyList(),
            visible = true,
            selectedKey = null,
            currentKey = null,
            onDismiss = {},
            onKeySelected = {},
            onConnect = {},
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