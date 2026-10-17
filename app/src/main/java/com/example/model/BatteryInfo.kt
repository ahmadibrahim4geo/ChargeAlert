package com.example.model

data class BatteryInfo(
    val level: Int = -1,
    val isCharging: Boolean = false,
    val isPowerConnected: Boolean = isCharging,
    val plugType: String = "Not Charging",
    val voltage: Int = 0,
    val temperature: Float = Float.NaN,
    val health: String = "Unknown",
    val technology: String = "Unknown",
    val currentNowMa: Int = 0,
    val hasCurrentReading: Boolean = false,
    val wattage: Float = 0f,
    val chargingSpeedCategory: String = "Normal",
    val estimatedMinutesRemaining: Int = -1
)
