@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.supersmartkeyapp.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.ui.theme.AppTheme


@Composable
fun HomeTopAppBar(onSettings: () -> Unit) {
    CenterAlignedTopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = {
        IconButton(onClick = onSettings) {
            Icon(Icons.Rounded.Settings, stringResource(R.string.settings))
        }
    }, modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SettingsTopAppBar(onBack: () -> Unit) {
    TopAppBar(title = { Text(stringResource(R.string.settings)) }, navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back))
        }
    }, modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
private fun HomeTopAppBarPreview() {
    AppTheme {
        Surface {
            HomeTopAppBar {}
        }
    }
}

@Preview
@Composable
private fun SettingsTopAppBarPreview() {
    AppTheme {
        Surface {
            SettingsTopAppBar {}
        }
    }
}