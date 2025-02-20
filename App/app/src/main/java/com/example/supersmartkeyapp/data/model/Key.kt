package com.example.supersmartkeyapp.data.model

data class Key(
    val name: String,
    val address: String,
    val lastSeen: Long?,
    val rssi: Int?,
)