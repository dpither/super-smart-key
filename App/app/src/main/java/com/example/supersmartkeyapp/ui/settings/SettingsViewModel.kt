package com.example.supersmartkeyapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supersmartkeyapp.data.ServiceRepository
import com.example.supersmartkeyapp.util.DEFAULT_GRACE_PERIOD
import com.example.supersmartkeyapp.util.DEFAULT_POLLING_RATE
import com.example.supersmartkeyapp.util.DEFAULT_RSSI_THRESHOLD
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
    val isLoading: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
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
            serviceRepository.rssiThreshold.collect { rssiThreshold ->
                _uiState.update {
                    it.copy(rssiThreshold = rssiThreshold)
                }
            }
        }

        viewModelScope.launch {
            serviceRepository.gracePeriod.collect { gracePeriod ->
                _uiState.update {
                    it.copy(gracePeriod = gracePeriod)
                }
            }
        }

        viewModelScope.launch {
            serviceRepository.pollingRate.collect { pollingRate ->
                _uiState.update {
                    it.copy(
                        pollingRate = pollingRate,
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
            serviceRepository.updateRssiThreshold(uiState.value.rssiThreshold)
        }
    }

    fun updateGracePeriod(gracePeriod: Int) {
        _uiState.update {
            it.copy(gracePeriod = gracePeriod)
        }
    }

    fun saveGracePeriod() {
        viewModelScope.launch {
            serviceRepository.updateGracePeriod(uiState.value.gracePeriod)
        }
    }

    fun updatePollingRate(pollingRate: Int) {
        _uiState.update {
            it.copy(pollingRate = pollingRate)
        }
    }

    fun savePollingRate() {
        viewModelScope.launch {
            serviceRepository.updatePollingRate(uiState.value.pollingRate)
        }
    }
}