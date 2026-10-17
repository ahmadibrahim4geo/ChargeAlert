package com.example.model

data class AlertSettings(
    val targetPercentage: Int = 80,
    val isAlertEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val repeatNotification: Boolean = false,
    val lastTriggeredPercentage: Int = -1,
    val soundType: String = "ALARM",
    val isLockscreenEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val lockscreenThemeId: String = "NEON_RING",
    val isAlwaysOnMonitorEnabled: Boolean = false,
    val language: String = "ar",
    val isOemAutoStartAcknowledged: Boolean = false,

    // ── Battery Safety Suite (Phase 2 & 3) ──────────────────────────────────
    val isLowBatteryAlertEnabled: Boolean = true,
    val lowBatteryPercentage: Int = 20,
    val lastTriggeredLowPct: Int = -1,

    val isTemperatureAlertEnabled: Boolean = true,
    val temperatureThreshold: Int = 42,
    val lastTriggeredTemp: Float = -1f,

    val isVoiceAlertEnabled: Boolean = false
)
