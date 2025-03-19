package com.example.supersmartkeyapp.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.ui.SettingsTopAppBar
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
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val gradientBrush = Brush.linearGradient(
        colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.secondary),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawRect(gradientBrush) },
        containerColor = Color.Transparent,
        topBar = { SettingsTopAppBar(onBack) },
    ) { paddingValues ->
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
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
    Column(modifier = modifier.fillMaxSize()) {
        HorizontalDivider()
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .width(240.dp)
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
//            RSSI threshold slider
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
//            Grace period slider
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
//            Polling rate slider
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
    val colors = SliderDefaults.colors(
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row {
            Text(text = "$title: ", fontWeight = FontWeight.SemiBold)
            Text(text = "$value $units")
        }
        val labelWidth = 48.dp
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "$minVal", modifier = Modifier.width(labelWidth))
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.roundToInt()) },
                onValueChangeFinished = onValueChangeFinished,
                valueRange = minVal.toFloat()..maxVal.toFloat(),
                steps = steps,
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            Text(text = "$maxVal", textAlign = TextAlign.End, modifier = Modifier.width(labelWidth))
        }
    }
}

@PreviewScreenSizes
@Composable
fun SettingsContentPreview() {
    AppTheme {
        Surface {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { SettingsTopAppBar(onBack = {}) },
            ) { paddingValues ->
                SettingsContent(rssiThreshold = DEFAULT_RSSI_THRESHOLD,
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
                SettingsContent(rssiThreshold = DEFAULT_RSSI_THRESHOLD,
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