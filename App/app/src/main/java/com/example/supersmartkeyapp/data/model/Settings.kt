package com.example.supersmartkeyapp.data.model

data class Settings(
    val rssiThreshold: Int,
    val gracePeriod: Int,
    val pollingRateSeconds: Int,
)