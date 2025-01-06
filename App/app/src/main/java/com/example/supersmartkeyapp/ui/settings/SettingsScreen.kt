package com.example.supersmartkeyapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
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
import com.example.supersmartkeyapp.util.DEFAULT_GRACE_PERIOD
import com.example.supersmartkeyapp.util.DEFAULT_POLLING_RATE
import com.example.supersmartkeyapp.util.DEFAULT_RSSI_THRESHOLD
import com.example.supersmartkeyapp.util.MAX_GRACE_PERIOD
import com.example.supersmartkeyapp.util.MAX_POLLING_RATE
import com.example.supersmartkeyapp.util.MAX_RSSI_THRESHOLD
import com.example.supersmartkeyapp.util.MIN_GRACE_PERIOD
import com.example.supersmartkeyapp.util.MIN_POLLING_RATE
import com.example.supersmartkeyapp.util.MIN_RSSI_THRESHOLD
import com.example.supersmartkeyapp.util.SettingsTopAppBar
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { SettingsTopAppBar(onBack) },
    ) { paddingValues ->

        SettingsContent(
            rssiThreshold = uiState.rssiThreshold,
            onRssiThresholdChange = viewModel::updateRssiThreshold,
            saveRssiThreshold = viewModel::saveRssiThreshold,
//            TODO: Update viewmodel
            gracePeriod = 0,
            onGracePeriodChange = { },
            saveGracePeriod = { },
            pollingRate = 0,
            onPollingRateChange = { },
            savePollingRate = { },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun SettingsContent(
    rssiThreshold: Int,
    onRssiThresholdChange: (Int) -> Unit,
    saveRssiThreshold: () -> Unit,
    gracePeriod: Int,
    onGracePeriodChange: (Int) -> Unit,
    saveGracePeriod: () -> Unit,
    pollingRate: Int,
    onPollingRateChange: (Int) -> Unit,
    savePollingRate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
//        RSSI THRESHOLD
        SettingsIntSlider(
            title = "RSSI Threshold",
            units = "dBm",
            value = rssiThreshold,
            onValueChange = onRssiThresholdChange,
            onValueChangeFinished = saveRssiThreshold,
            minVal = MIN_RSSI_THRESHOLD,
            maxVal = MAX_RSSI_THRESHOLD,
            steps = (MAX_RSSI_THRESHOLD - MIN_RSSI_THRESHOLD) - 1
        )
        HorizontalDivider()
        SettingsIntSlider(
            title = "Grace Period",
            units = "s",
            value = gracePeriod,
            onValueChange = onGracePeriodChange,
            onValueChangeFinished = saveGracePeriod,
            minVal = MIN_GRACE_PERIOD,
            maxVal = MAX_GRACE_PERIOD,
            steps = (MAX_GRACE_PERIOD - MIN_GRACE_PERIOD) - 1
        )
        HorizontalDivider()
        SettingsIntSlider(
            title = "Polling Rate",
            units = "s",
            value = pollingRate,
            onValueChange = onPollingRateChange,
            onValueChangeFinished = savePollingRate,
            minVal = MIN_POLLING_RATE,
            maxVal = MAX_POLLING_RATE,
            steps = (MAX_POLLING_RATE - MIN_POLLING_RATE) - 1
        )
    }
}

@Composable
private fun SettingsIntSlider(
    title: String,
    units: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: () -> Unit,
    minVal: Int,
    maxVal: Int,
    steps: Int
) {
    val colors = SliderDefaults.colors(
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = 16.dp)
    )
    Text(
        text = "$value $units",
        style = MaterialTheme.typography.bodySmall,
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            "$minVal",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 4.dp)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            onValueChangeFinished = onValueChangeFinished,
            valueRange = minVal.toFloat()..maxVal.toFloat(),
            steps = steps,
            colors = colors,
            modifier = Modifier.width(300.dp)
        )
        Text(
            "$maxVal",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Preview
@Composable
fun SettingsContentPreview() {
    AppTheme {
        Surface {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { SettingsTopAppBar(onBack = {}) },
            ) { paddingValues ->
                SettingsContent(
                    rssiThreshold = DEFAULT_RSSI_THRESHOLD,
                    onRssiThresholdChange = {},
                    saveRssiThreshold = {},
                    gracePeriod = DEFAULT_GRACE_PERIOD,
                    onGracePeriodChange = {},
                    saveGracePeriod = {},
                    pollingRate = DEFAULT_POLLING_RATE,
                    onPollingRateChange = {},
                    savePollingRate = {},
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}