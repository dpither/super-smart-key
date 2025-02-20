package com.example.supersmartkeyapp.ui.home

import android.util.Log
import androidx.compose.runtime.key
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supersmartkeyapp.data.manager.KeyServiceManager
import com.example.supersmartkeyapp.data.repository.KeyRepository
import com.example.supersmartkeyapp.data.repository.ServiceRepository
import com.example.supersmartkeyapp.data.model.Key
import com.example.supersmartkeyapp.util.DEFAULT_RSSI_THRESHOLD
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLockServiceRunning: Boolean = false,
    val isKeyConnected: Boolean = false,
    val key: Key? = null,
    val selectedKey: Key? = null,
    val rssi: Int? = null,
    val rssiThreshold: Int = DEFAULT_RSSI_THRESHOLD,
    val availableKeys: List<Key> = emptyList(),
    val showAvailableKeysDialog: Boolean = false,
    val showBluetoothPermissionDialog: Boolean = false,
    val showDeviceAdminDialog: Boolean = false,
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
        loadUiState()
        keyServiceManager.bindToServiceIfRunning()
    }

    private fun loadUiState() {
        _uiState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            settingsRepository.isLockServiceRunningFlow.collect { value ->
                _uiState.update {
                    it.copy(isLockServiceRunning = value)
                }
            }
        }

        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { value ->
                _uiState.update {
                    it.copy(rssiThreshold = value.rssiThreshold)
                }
            }
        }

        viewModelScope.launch {
            keyRepository.key.collect { value ->
                _uiState.update {
                    it.copy(key = value)
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

        viewModelScope.launch {
            keyRepository.isConnected.collect { value ->
                _uiState.update {
                    it.copy(isKeyConnected = value)
                }
            }
        }
    }

    fun startLockService() {
        keyServiceManager.startLockService()
    }

    fun stopLockService() {
        keyServiceManager.stopLockService()
    }

    fun selectKey(key: Key) {
        _uiState.update {
            it.copy(selectedKey = key)
        }
    }

    fun connectKey() {
        if (_uiState.value.selectedKey?.address != _uiState.value.key?.address) {
            keyServiceManager.stopLockService()
            keyServiceManager.startKeyService()
            viewModelScope.launch {
                _uiState.value.selectedKey?.let { keyRepository.connectKey(it) }
            }
        }
    }

    fun disconnectKey() {
        keyServiceManager.stopKeyService()
        viewModelScope.launch {
            keyRepository.disconnectKey()
        }
    }

    fun openAvailableKeysDialog() {
        keyRepository.refreshAvailableKeys()
        _uiState.update {
            it.copy(showAvailableKeysDialog = true, selectedKey = _uiState.value.key)
        }
    }

    fun closeAvailableKeysDialog() {
        _uiState.update {
            it.copy(showAvailableKeysDialog = false)
        }
    }

    fun openBluetoothPermissionDialog() {
        _uiState.update {
            it.copy(showBluetoothPermissionDialog = true)
        }
    }

    fun closeBluetoothPermissionDialog() {
        _uiState.update {
            it.copy(showBluetoothPermissionDialog = false)
        }
    }

    fun openDeviceAdminDialog() {
        _uiState.update {
            it.copy(showDeviceAdminDialog = true)
        }
    }

    fun closeDeviceAdminDialog() {
        _uiState.update {
            it.copy(showDeviceAdminDialog = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!_uiState.value.isLockServiceRunning) {
            keyServiceManager.stopKeyService()
        } else {
            keyServiceManager.unbind()
        }
    }
}