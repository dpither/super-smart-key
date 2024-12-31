package com.example.supersmartkeyapp.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.supersmartkeyapp.ui.theme.AppTheme
import com.example.supersmartkeyapp.util.MAX_RSSI_THRESHOLD
import com.example.supersmartkeyapp.util.MIN_RSSI_THRESHOLD
import com.example.supersmartkeyapp.util.SettingsTopAppBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { SettingsTopAppBar(onBack) },
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        SettingsContent(
            rssiThreshold = uiState.rssiThreshold,
            onRssiThresholdChanged = viewModel::updateRssiThreshold,
            saveRssiThreshold = viewModel::saveRssiThreshold,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun SettingsContent(
    rssiThreshold: Int,
    onRssiThresholdChanged: (Int) -> Unit,
    saveRssiThreshold: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        SettingRSSIThreshold(
            rssiThreshold = rssiThreshold,
            onRssiThresholdChanged = onRssiThresholdChanged,
            saveRssiThreshold = saveRssiThreshold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingRSSIThreshold(
    rssiThreshold: Int,
    onRssiThresholdChanged: (Int) -> Unit,
    saveRssiThreshold: () -> Unit
) {
    val colors = SliderDefaults.colors(
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
    Column {
        Text(
            "RSSI Threshold: $rssiThreshold dBm",
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "$MIN_RSSI_THRESHOLD",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(end = 4.dp)
            )
            Slider(
                value = rssiThreshold.toFloat(),
                onValueChange = { onRssiThresholdChanged(it.toInt()) },
                onValueChangeFinished = saveRssiThreshold,
                valueRange = MIN_RSSI_THRESHOLD.toFloat()..MAX_RSSI_THRESHOLD.toFloat(),
                steps = MAX_RSSI_THRESHOLD - MIN_RSSI_THRESHOLD - 1,
                track = { sliderState ->
                    SliderDefaults.Track(
                        colors = colors,
                        sliderState = sliderState
                    )
                },
                modifier = Modifier.weight(1f)
            )
            Text(
                "$MAX_RSSI_THRESHOLD",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Preview
@Composable
fun SettingsContentPreview() {
    AppTheme {
        Surface {
            SettingsContent(-30, {}, {})
        }
    }
}