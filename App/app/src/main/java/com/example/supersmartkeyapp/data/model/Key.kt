package com.example.supersmartkeyapp.data.model

import android.bluetooth.BluetoothDevice

data class Key(
    val name: String,
    val device: BluetoothDevice,
    val rssi: Int?,
)