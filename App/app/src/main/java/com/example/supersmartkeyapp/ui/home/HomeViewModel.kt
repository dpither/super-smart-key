package com.example.supersmartkeyapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supersmartkeyapp.data.manager.KeyServiceManager
import com.example.supersmartkeyapp.data.repository.KeyRepository
import com.example.supersmartkeyapp.data.repository.ServiceRepository
import com.example.supersmartkeyapp.data.model.Key
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
    private val settingsRepository: ServiceRepository,
    private val keyRepository: KeyRepository,
    private val keyServiceManager: KeyServiceManager
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
            settingsRepository.isServiceRunningFlow.collect { value ->
                _uiState.update {
                    it.copy(isServiceRunning = value)
                }
            }
        }

        viewModelScope.launch {
            keyRepository.availableKeys.collect { value ->
                _uiState.update {
                    it.copy(
                        availableKeys = value, isLoading = false
                    )
                }
            }
        }
    }

    fun runKeyService() {
        keyServiceManager.runKeyService()
    }

    fun pauseKeyService() {
        keyServiceManager.pauseKeyService()
    }

    fun linkKey(key: Key) {
//        Maybe link by using selected index and available keys
        if (key.device.address != _uiState.value.key?.device?.address) {
            pauseKeyService()
            keyServiceManager.startKeyService()
            viewModelScope.launch {
                keyRepository.link(key)
            }
        }
    }

    fun unlinkKey() {
        if (_uiState.value.key != null) {
            keyServiceManager.stopKeyService()
            viewModelScope.launch {
                keyRepository.unlink()
            }
        }
    }

    fun openDialog() {
        _uiState.update {
            it.copy(showDialog = true)
        }
    }

    fun closeDialog() {
        _uiState.update {
            it.copy(showDialog = false)
        }
    }
}