package com.example.supersmartkeyapp.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
            isLoading = uiState.isLoading,
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
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HorizontalDivider()
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .width(240.dp)
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            SettingsIntSlider(
                title = stringResource(R.string.rssi_threshold),
                units = stringResource(R.string.rssi_threshold_units),
                value = rssiThreshold,
                onValueChange = onRssiThresholdChange,
                onValueChangeFinished = onRssiThresholdChangeFinished,
                minVal = MIN_RSSI_THRESHOLD,
                maxVal = MAX_RSSI_THRESHOLD,
                steps = (MAX_RSSI_THRESHOLD - MIN_RSSI_THRESHOLD) - 1,
            )
            HorizontalDivider()
            SettingsIntSlider(
                title = stringResource(R.string.grace_period),
                units = stringResource(R.string.grace_period_units),
                value = gracePeriod,
                onValueChange = onGracePeriodChange,
                onValueChangeFinished = onGracePeriodChangeFinished,
                minVal = MIN_GRACE_PERIOD,
                maxVal = MAX_GRACE_PERIOD,
                steps = (MAX_GRACE_PERIOD - MIN_GRACE_PERIOD) - 1,
            )
            HorizontalDivider()
            SettingsIntSlider(
                title = stringResource(R.string.polling_rate),
                units = stringResource(R.string.polling_rate_units),
                value = pollingRate,
                onValueChange = onPollingRateChange,
                onValueChangeFinished = onPollingRateChangeFinished,
                minVal = MIN_POLLING_RATE,
                maxVal = MAX_POLLING_RATE,
                steps = (MAX_POLLING_RATE - MIN_POLLING_RATE) - 1,
            )
            HorizontalDivider()
        }

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
    val titleStyle = MaterialTheme.typography.titleMedium
    val textStyle = MaterialTheme.typography.bodyLarge
    val colors = SliderDefaults.colors(
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = titleStyle,
        )
        Text(
            text = "$value $units",
            style = textStyle,
        )
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "$minVal", style = textStyle, modifier = Modifier.align(Alignment.CenterStart)
            )
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.roundToInt()) },
                onValueChangeFinished = onValueChangeFinished,
                valueRange = minVal.toFloat()..maxVal.toFloat(),
                steps = steps,
                colors = colors,
                modifier = Modifier.width(240.dp)
            )
            Text(
                "$maxVal", style = textStyle, modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
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
                    isLoading = false,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsContentLoadingPreview() {
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
                    isLoading = true,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}