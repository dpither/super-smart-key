/*
    Copyright (C) 2025  Dylan Pither

    This file is part of Super Smart Key.

    Super Smart Key is free software: you can redistribute it and/or modify it under the terms of
    the GNU General Public License as published by the Free Software Foundation, either version 3
    of the License, or (at your option) any later version.

    Super Smart Key is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with Super Smart Key.
    If not, see <https://www.gnu.org/licenses/>.
 */

package com.dpither.supersmartkey.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dpither.supersmartkey.R
import com.dpither.supersmartkey.data.model.Key
import com.dpither.supersmartkey.ui.theme.AppTheme
import com.dpither.supersmartkey.util.DEFAULT_ANIMATION_DURATION
import com.dpither.supersmartkey.util.FONT_SCALE_CAP

@Composable
fun PermissionRationaleDialog(
    visible: Boolean,
    title: String,
    rationale: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    CustomDialog(showDialog = visible, onDismissRequest = onDismiss) {
        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.secondary
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind { drawRect(gradientBrush) },
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
//                Title
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
//                Rationale
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f, fill = false)
                ) {
                    item {
                        Text(
                            text = rationale,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
//                    Cancel Button
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
//                    OK Button
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
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
    CustomDialog(showDialog = visible, onDismissRequest = onDismiss) {
        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.secondary
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind { drawRect(gradientBrush) },
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
//                Title
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.key_dialog_title),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                HorizontalDivider()
//                    List
                if (availableKeys.isEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.empty_key_dialog_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = stringResource(R.string.empty_key_dialog_text),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(availableKeys) { key ->
                            KeyRow(name = key.name,
                                address = key.address,
                                selected = key.address == selectedKey?.address,
                                onClick = { onKeySelected(key) })
                        }
                    }
                }

                Row(modifier = Modifier.padding(16.dp)) {
//                    Cancel Button
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    if (selectedKey != null && selectedKey.address == currentKey?.address) {
//                        If selected key is the current key Unlink Button
                        Button(onClick = {
                            onDisconnect()
                            onDismiss()
                        }, modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.unlink))
                        }
                    } else {
//                        Else Link Button, enabled only if a key selected
                        Button(enabled = selectedKey != null, onClick = {
                            onConnect()
                            onDismiss()
                        }, modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.link))
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
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth()
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
                text = address,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CustomDialog(
    showDialog: Boolean, onDismissRequest: () -> Unit, content: @Composable () -> Unit
) {
//    Adapted from: https://gist.github.com/sinasamaki/daa825d96235a18822177a2b1b323f49?ref=sinasamaki.com

    var showAnimatedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showDialog) {
        if (showDialog) showAnimatedDialog = true
    }

    if (showAnimatedDialog) {
        Dialog(
            onDismissRequest = onDismissRequest,
        ) {
            val cappedDensity = Density(
                density = LocalDensity.current.density,
                fontScale = LocalDensity.current.fontScale.coerceAtMost(FONT_SCALE_CAP)
            )
            CompositionLocalProvider(
                LocalDensity provides cappedDensity,
                LocalContentColor provides MaterialTheme.colorScheme.onBackground
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
                                .padding(16.dp)
                                .pointerInput(Unit) { detectTapGestures { } }
                                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
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
}

@Preview
@Composable
private fun PermissionRationaleDialogPreview() {
    AppTheme {
        Surface {
            PermissionRationaleDialog(visible = true,
                title = stringResource(R.string.api_31_bluetooth_title),
                rationale = stringResource(R.string.api_31_bluetooth_denied_rationale),
                onConfirm = {},
                onDismiss = {})
        }
    }
}

@Preview
@Composable
private fun AvailableKeysDialogEmptyPreview() {
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
private fun AvailableKeysDialogPreview() {
    val availableKeys = listOf(
        Key("Smart Tag2", "00:11:22:AA:BB:CD", null, null, false),
        Key("Smart Tag2", "00:11:22:AA:BB:CE", null, null, false),
        Key("Smart Tag2", "00:11:22:AA:BB:CF", null, null, false),
        Key("Smart Tag2", "00:11:22:AA:BB:CG", null, null, false),
        Key("Smart Tag2", "00:11:22:AA:BB:CH", null, null, false),
        Key("Smart Tag2", "00:11:22:AA:BB:CI", null, null, false),
        Key("Smart Tag2", "00:11:22:AA:BB:CJ", null, null, false),
        Key("Smart Tag2", "00:11:22:AA:BB:CK", null, null, false),
        Key("Smart Tag2", "00:11:22:AA:BB:CL", null, null, false)
    )
    AppTheme {
        AvailableKeysDialog(availableKeys = availableKeys,
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