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

package com.dpither.supersmartkey.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dpither.supersmartkey.data.manager.KeyServiceManager
import com.dpither.supersmartkey.data.model.Key
import com.dpither.supersmartkey.data.repository.KeyRepository
import com.dpither.supersmartkey.data.repository.ServiceRepository
import com.dpither.supersmartkey.util.DEFAULT_RSSI_THRESHOLD
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
                        availableKeys = value.values.toList(), isLoading = false
                    )
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
//        Only need to try to connect when selected key is different from current key
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