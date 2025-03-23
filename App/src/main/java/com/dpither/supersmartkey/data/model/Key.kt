package com.dpither.supersmartkey.data.model

data class Key(
    val name: String,
    val address: String,
    val lastSeen: Long?,
    val rssi: Int?,
    val connected: Boolean,
)