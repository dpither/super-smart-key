package com.dpither.supersmartkey.data.model

data class Settings(
    val rssiThreshold: Int,
    val gracePeriod: Int,
    val pollingRateSeconds: Int,
)