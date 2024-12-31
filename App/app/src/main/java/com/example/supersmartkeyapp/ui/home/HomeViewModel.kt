package com.example.supersmartkeyapp.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supersmartkeyapp.data.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isKeyLinked: Boolean = false,
    val isServiceRunning: Boolean = false,
    val deviceAddress: String = "",
    val deviceName: String = "",
    val rssi: Int = 0,
    val showDialog: Boolean = false,
    val isLoading: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadUiState()
    }

    fun updateIsServiceRunning(isServiceRunning: Boolean) {
        viewModelScope.launch {
            serviceRepository.updateIsServiceRunning(isServiceRunning)
        }
    }

    fun updateIsKeyLinked(isKeyLinked: Boolean) {
        viewModelScope.launch {
            serviceRepository.updateIsKeyLinked(isKeyLinked)
        }
    }

    @SuppressLint("MissingPermission")
    fun updateDevice(device: BluetoothDevice) {
        Log.d("HOME VIEW", "updating $device")
        viewModelScope.launch {
            serviceRepository.updateDeviceAddress(device.address)
            if (device.name != null) {
                serviceRepository.updateDeviceName(device.name)
            } else {
                serviceRepository.updateDeviceName("")
            }
        }
    }

    fun updateShowDialog(showDialog: Boolean) {
        _uiState.update {
            it.copy(showDialog=showDialog)
        }
    }

    private fun loadUiState() {
        _uiState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            combine(
                serviceRepository.isKeyLinked,
                serviceRepository.isServiceRunning,
                serviceRepository.deviceAddress,
                serviceRepository.deviceName,
                serviceRepository.rssi,
            ) { isKeyLinked, isServiceRunning, deviceAddress, deviceName, rssi ->
                listOf(isKeyLinked, isServiceRunning, deviceAddress, deviceName, rssi)
            }.collect { (isKeyLinked, isServiceRunning, deviceAddress, deviceName, rssi) ->
                _uiState.update {
                    it.copy(
                        isKeyLinked = isKeyLinked as Boolean,
                        isServiceRunning = isServiceRunning as Boolean,
                        deviceAddress = deviceAddress as String,
                        deviceName = deviceName as String,
                        rssi = rssi as Int,
                        isLoading = false
                    )
                }
            }
        }
    }

}