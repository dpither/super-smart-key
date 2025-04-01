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

package com.dpither.supersmartkey.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dpither.supersmartkey.data.repository.ServiceRepository
import com.dpither.supersmartkey.util.DEFAULT_GRACE_PERIOD
import com.dpither.supersmartkey.util.DEFAULT_POLLING_RATE
import com.dpither.supersmartkey.util.DEFAULT_RSSI_THRESHOLD
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val rssiThreshold: Int = DEFAULT_RSSI_THRESHOLD,
    val gracePeriod: Int = DEFAULT_GRACE_PERIOD,
    val pollingRate: Int = DEFAULT_POLLING_RATE,
    val isLoading: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: ServiceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadUiState()
    }

    private fun loadUiState() {
        _uiState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        rssiThreshold = settings.rssiThreshold,
                        gracePeriod = settings.gracePeriod,
                        pollingRate = settings.pollingRateSeconds,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateRssiThreshold(rssiThreshold: Int) {
        _uiState.update {
            it.copy(rssiThreshold = rssiThreshold)
        }
    }

    fun saveRssiThreshold() {
        viewModelScope.launch {
            settingsRepository.updateRssiThreshold(uiState.value.rssiThreshold)
        }
    }

    fun updateGracePeriod(gracePeriod: Int) {
        _uiState.update {
            it.copy(gracePeriod = gracePeriod)
        }
    }

    fun saveGracePeriod() {
        viewModelScope.launch {
            settingsRepository.updateGracePeriod(uiState.value.gracePeriod)
        }
    }

    fun updatePollingRate(pollingRate: Int) {
        _uiState.update {
            it.copy(pollingRate = pollingRate)
        }
    }

    fun savePollingRate() {
        viewModelScope.launch {
            settingsRepository.updatePollingRate(uiState.value.pollingRate)
        }
    }
}