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
import com.example.supersmartkeyapp.util.BLE_HCI_CONNECTION_TIMEOUT
import com.example.supersmartkeyapp.util.MAX_RSSI
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    private var bluetoothAdapter = bluetoothManager.adapter

    fun refreshAvailableKeys() {
        _availableKeys.update { emptyList() }
        val newList = mutableListOf<Key>()
        bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).forEach { device ->
            newList.add(
                Key(
                    name = device.name ?: NULL_DEVICE_NAME,
                    address = device.address,
                    lastSeen = null,
                    rssi = null,
                    connected = false
                )
            )
        }
        _availableKeys.update { newList }
    }

    fun connectKey(key: Key) {
        if (_key.value?.connected == true) {
            disconnectKey()
        }

        val bluetoothGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    when (newState) {
                        BluetoothGatt.STATE_CONNECTED -> {
                            Log.d(TAG, "Connected to GATT server: ${gatt.device}")
                            bluetoothGatt = gatt
                            _key.update { it?.copy(connected = true) }
                        }

                        BluetoothGatt.STATE_DISCONNECTED -> {
                            Log.d(TAG, "Disconnected from GATT server: ${gatt.device}")
                            bluetoothGatt = null
                            _key.update { it?.copy(connected = false, rssi = MAX_RSSI) }
                        }
                        else -> Log.e(TAG, "GATT connection newState invalid.")
                    }
                } else {
                    when (status) {
                        BLE_HCI_CONNECTION_TIMEOUT -> {
                            Log.e(TAG, "Connection Timeout")
                            _key.update { it?.copy(connected = false, rssi = MAX_RSSI) }
                        }
                        else -> Log.e(TAG, "GATT connection state change operation failed, status: $status")
                    }

                }
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
                super.onReadRemoteRssi(gatt, rssi, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val currentTime = System.currentTimeMillis()
                    _key.update { it?.copy(lastSeen = currentTime, rssi = rssi) }
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(
                        ZoneId.systemDefault())
                    val date = formatter.format(Instant.ofEpochMilli(currentTime))
                    Log.d(TAG, "RSSI read success: $rssi at $date")
                } else {
                    Log.e(TAG, "GATT RSSI read failed, status: $status")
                }
            }
        }
        _key.update { key }
        bluetoothAdapter.getRemoteDevice(key.address)
            .connectGatt(context, true, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnectKey() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        _key.update { null }
    }

    fun readRemoteRssi() {
        bluetoothGatt?.readRemoteRssi() ?: Log.e(TAG, "GATT null: Trying to request RSSI")
    }
}