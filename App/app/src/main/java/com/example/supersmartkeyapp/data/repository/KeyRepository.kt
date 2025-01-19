package com.example.supersmartkeyapp.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.example.supersmartkeyapp.data.model.Key
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "KEY_REPO"
private const val NULL_DEVICE_NAME = "Unnamed Device"

@SuppressLint("MissingPermission")
@Singleton
class KeyRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val _key = MutableStateFlow<Key?>(null)
    val key: Flow<Key?> = _key

    private val _availableKeys = MutableStateFlow(emptyList<Key>())
    val availableKeys: Flow<List<Key>> = _availableKeys

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothGatt: BluetoothGatt? = null

    fun refreshAvailableKeys() {
        _availableKeys.update { emptyList() }
        val newList = mutableListOf<Key>()
        bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).forEach { device ->
            newList.add(
                Key(
                    name = device.name ?: NULL_DEVICE_NAME, device = device, rssi = null
                )
            )
        }
        _availableKeys.update { newList }
    }

    fun link(key: Key) {
        if (_key.value != null) {
            unlink()
        }
        val device: BluetoothDevice = key.device
        val bluetoothGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    when (newState) {
                        BluetoothGatt.STATE_CONNECTED -> {
                            Log.d(TAG, "Connected to GATT server: ${gatt.device}")
                            bluetoothGatt = gatt
                            _key.update {
                                Key(
                                    name = device.name ?: NULL_DEVICE_NAME,
                                    device = device,
                                    rssi = null
                                )
                            }
                        }

                        BluetoothGatt.STATE_DISCONNECTED -> {
                            Log.d(TAG, "Disconnected from GATT server: ${gatt.device}")
                            bluetoothGatt = null
                            _key.update { null }
                        }
                    }
                } else {
                    Log.e(TAG, "GATT connection state change operation failed, status: $status")
                }
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
                super.onReadRemoteRssi(gatt, rssi, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "RSSI read success: $rssi")
                    _key.update { it?.copy(rssi = rssi) }
                } else {
                    Log.e(TAG, "GATT RSSI read failed, status: $status")
                }
            }
        }
//        TODO: Figure out if i should autoreconnect
        device.connectGatt(context, false, bluetoothGattCallback)
    }

    fun unlink() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        _key.update { null }
    }

    fun readRemoteRssi() {
        Log.d(TAG, "Requesting RSSI")
        bluetoothGatt?.readRemoteRssi() ?: Log.e(TAG, "GATT null: Trying to request RSSI")
    }
}