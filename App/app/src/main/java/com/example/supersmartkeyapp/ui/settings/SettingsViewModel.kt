package com.example.supersmartkeyapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supersmartkeyapp.data.SettingsRepository
import com.example.supersmartkeyapp.util.DEFAULT_RSSI_THRESHOLD
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val rssiThreshold: Int = DEFAULT_RSSI_THRESHOLD,
    val isLoading: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {


    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            settingsRepository.rssiThreshold.collect { rssiThreshold ->
                _uiState.update {
                    it.copy(
                        rssiThreshold = rssiThreshold,
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
            settingsRepository.updateRSSIThreshold(uiState.value.rssiThreshold)
        }
    }
}