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

package com.dpither.supersmartkey.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.IntentCompat
import com.dpither.supersmartkey.data.model.Key
import com.dpither.supersmartkey.util.BLE_HCI_CONNECTION_TIMEOUT
import com.dpither.supersmartkey.util.MAX_RSSI
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    private val _availableKeys = MutableStateFlow(hashMapOf<String, Key>())
    val availableKeys: Flow<HashMap<String, Key>> = _availableKeys

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    private val nameReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_NAME_CHANGED -> {
                    val device = IntentCompat.getParcelableExtra(
                        intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                    )
                    Log.d(TAG, "ACTION NAME CHANGE ${device?.address}: ${device?.name}")
                    if (_availableKeys.value.containsKey(device?.address) && device?.name != null) {
                        _availableKeys.value = HashMap(_availableKeys.value).apply {
                            this[device.address] = this[device.address]?.copy(name = device.name)
                        }
                        if (_key.value?.address == device.address) {
                            _key.update { it?.copy(name = device.name) }
                        }
                    }
                }
            }
        }
    }
    private val nameFilter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_NAME_CHANGED)
    }

    init {
        context.registerReceiver(nameReceiver, nameFilter)
    }

    fun refreshAvailableKeys() {
        _availableKeys.update { hashMapOf() }

        val newMap = hashMapOf<String, Key>()
        bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).forEach { device ->
            if (device.name == null) {
                getDeviceName(device)
            }
            newMap[device.address] = Key(
                name = device.name ?: NULL_DEVICE_NAME,
                address = device.address,
                lastSeen = null,
                rssi = null,
                connected = false
            )
        }

        _availableKeys.update { newMap }
    }

    fun connectKey(key: Key) {
        if (_key.value?.connected == true) {
            disconnectKey()
        }

        val bluetoothGattCallback = object : BluetoothGattCallback() {
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                Log.d(TAG, "SERVICE DISCOVERED")
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    when (newState) {
                        BluetoothGatt.STATE_CONNECTED -> {
                            Log.d(TAG, "Connected to GATT server: ${gatt.device.name}")
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

                        else -> Log.e(
                            TAG, "GATT connection state change operation failed, status: $status"
                        )
                    }
                }
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
                super.onReadRemoteRssi(gatt, rssi, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val currentTime = System.currentTimeMillis()
                    _key.update { it?.copy(lastSeen = currentTime, rssi = rssi) }
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(
                        ZoneId.systemDefault()
                    )
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

    fun requestRemoteRssi() {
        bluetoothGatt?.readRemoteRssi() ?: Log.e(TAG, "GATT null: Trying to request RSSI")
    }

    //    Retrieve name if null, with a single retry policy
    private fun getDeviceName(device: BluetoothDevice) {
        Log.d(TAG, "${device.address} name is null, performing service discovery")
        device.fetchUuidsWithSdp()

        CoroutineScope(Dispatchers.IO).launch {
            delay(5000)
            if (_availableKeys.value[device.address]?.name == NULL_DEVICE_NAME) {
                Log.d(TAG, "${device.address} name is still null, retrying")
                device.fetchUuidsWithSdp()
            }
        }
    }
}