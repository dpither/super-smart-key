package com.example.supersmartkeyapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.supersmartkeyapp.R
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
            onRssiThresholdChangeFinished = viewModel::saveRssiThreshold,
            gracePeriod = uiState.gracePeriod,
            onGracePeriodChange = viewModel::updateGracePeriod,
            onGracePeriodChangeFinished = viewModel::saveGracePeriod,
            pollingRate = uiState.pollingRate,
            onPollingRateChange = viewModel::updatePollingRate,
            onPollingRateChangeFinished = viewModel::savePollingRate,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun SettingsContent(
    rssiThreshold: Int,
    onRssiThresholdChange: (Int) -> Unit,
    onRssiThresholdChangeFinished: () -> Unit,
    gracePeriod: Int,
    onGracePeriodChange: (Int) -> Unit,
    onGracePeriodChangeFinished: () -> Unit,
    pollingRate: Int,
    onPollingRateChange: (Int) -> Unit,
    onPollingRateChangeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SettingsIntSlider(
            title = stringResource(R.string.rssi_threshold_settings_title),
            units = stringResource(R.string.rssi_threshold_units),
            value = rssiThreshold,
            onValueChange = onRssiThresholdChange,
            onValueChangeFinished = onRssiThresholdChangeFinished,
            minVal = MIN_RSSI_THRESHOLD,
            maxVal = MAX_RSSI_THRESHOLD,
            steps = (MAX_RSSI_THRESHOLD - MIN_RSSI_THRESHOLD) - 1
        )
        SettingsIntSlider(
            title = stringResource(R.string.grace_period_settings_title),
            units = stringResource(R.string.grace_period_units),
            value = gracePeriod,
            onValueChange = onGracePeriodChange,
            onValueChangeFinished = onGracePeriodChangeFinished,
            minVal = MIN_GRACE_PERIOD,
            maxVal = MAX_GRACE_PERIOD,
            steps = (MAX_GRACE_PERIOD - MIN_GRACE_PERIOD) - 1
        )
        SettingsIntSlider(
            title = stringResource(R.string.polling_rate_settings_title),
            units = stringResource(R.string.polling_rate_units),
            value = pollingRate,
            onValueChange = onPollingRateChange,
            onValueChangeFinished = onPollingRateChangeFinished,
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
    steps: Int,
) {
    val colors = SliderDefaults.colors(
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "$value $units",
            style = MaterialTheme.typography.bodySmall,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                "$minVal",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterStart)
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
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
        HorizontalDivider()
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
                    onRssiThresholdChangeFinished = {},
                    gracePeriod = DEFAULT_GRACE_PERIOD,
                    onGracePeriodChange = {},
                    onGracePeriodChangeFinished = {},
                    pollingRate = DEFAULT_POLLING_RATE,
                    onPollingRateChange = {},
                    onPollingRateChangeFinished = {},
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}