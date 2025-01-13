package com.example.supersmartkeyapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supersmartkeyapp.data.Key
import com.example.supersmartkeyapp.data.KeyRepository
import com.example.supersmartkeyapp.data.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isServiceRunning: Boolean = false,
    val key: Key? = null,
    val rssi: Int? = null,
    val showDialog: Boolean = false,
    val availableKeys: List<Key> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val keyRepository: KeyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            keyRepository.refreshAvailableKeys()
        }
        loadUiState()
    }

    private fun loadUiState() {
        _uiState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            keyRepository.key.collect { value ->
                _uiState.update {
                    it.copy(key = value)
                }
            }
        }

        viewModelScope.launch {
            serviceRepository.isServiceRunning.collect { value ->
                _uiState.update {
                    it.copy(isServiceRunning = value)
                }
            }
        }

        viewModelScope.launch {
            keyRepository.availableKeys.collect { value ->
                _uiState.update {
                    it.copy(
                        availableKeys = value,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateIsServiceRunning(isServiceRunning: Boolean) {
//        TODO: Start service/stop service
        viewModelScope.launch {
            serviceRepository.updateIsServiceRunning(isServiceRunning)
        }
    }

    fun linkKey(key: Key) {
//        TODO: Init service? or maybe in repository
        viewModelScope.launch {
            keyRepository.linkDevice(key.device)
        }
    }

    fun updateShowDialog(showDialog: Boolean) {
        _uiState.update {
            it.copy(showDialog = showDialog)
        }
    }
}