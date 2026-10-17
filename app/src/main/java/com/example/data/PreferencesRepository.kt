package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AlertSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("charge_alert_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AlertSettings> = _settings.asStateFlow()

    private val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _settings.value = loadSettings()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)
    }

    fun loadSettings(): AlertSettings {
        return AlertSettings(
            targetPercentage = prefs.getInt(KEY_TARGET_PERCENTAGE, 80).coerceIn(50, 100),
            isAlertEnabled = prefs.getBoolean(KEY_ALERT_ENABLED, true),
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            vibrateEnabled = prefs.getBoolean(KEY_VIBRATE_ENABLED, true),
            repeatNotification = prefs.getBoolean(KEY_REPEAT_NOTIFICATION, false),
            lastTriggeredPercentage = prefs.getInt(KEY_LAST_TRIGGERED_PCT, -1),
            soundType = prefs.getString(KEY_SOUND_TYPE, "ALARM") ?: "ALARM",
            isLockscreenEnabled = prefs.getBoolean(KEY_LOCKSCREEN_ENABLED, true),
            animationsEnabled = prefs.getBoolean("animations_enabled", true),
            lockscreenThemeId = prefs.getString(KEY_LOCKSCREEN_THEME_ID, "NEON_RING") ?: "NEON_RING",
            isAlwaysOnMonitorEnabled = prefs.getBoolean(KEY_ALWAYS_ON_MONITOR, false),
            language = prefs.getString(KEY_LANGUAGE, "ar") ?: "ar",
            isOemAutoStartAcknowledged = prefs.getBoolean(KEY_OEM_AUTO_START_ACK, false),
            isLowBatteryAlertEnabled = prefs.getBoolean(KEY_LOW_BATTERY_ALERT_ENABLED, true),
            lowBatteryPercentage = prefs.getInt(KEY_LOW_BATTERY_PERCENTAGE, 20),
            lastTriggeredLowPct = prefs.getInt(KEY_LAST_TRIGGERED_LOW_PCT, -1),
            isTemperatureAlertEnabled = prefs.getBoolean(KEY_TEMP_ALERT_ENABLED, true),
            temperatureThreshold = prefs.getInt(KEY_TEMP_THRESHOLD, 42),
            lastTriggeredTemp = prefs.getFloat(KEY_LAST_TRIGGERED_TEMP, -1f),
            isVoiceAlertEnabled = prefs.getBoolean(KEY_VOICE_ALERT_ENABLED, false)
        )
    }

    fun updateAlwaysOnMonitorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALWAYS_ON_MONITOR, enabled).apply()
        _settings.value = _settings.value.copy(isAlwaysOnMonitorEnabled = enabled)
    }

    fun updateTargetPercentage(percentage: Int) {
        val validPct = percentage.coerceIn(50, 100)
        if (validPct == loadSettings().targetPercentage) return
        prefs.edit()
            .putInt(KEY_TARGET_PERCENTAGE, validPct)
            .putInt(KEY_LAST_TRIGGERED_PCT, -1)
            .apply()
        _settings.value = _settings.value.copy(targetPercentage = validPct, lastTriggeredPercentage = -1)
    }

    fun updateAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALERT_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isAlertEnabled = enabled)
    }

    fun updateSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(soundEnabled = enabled)
    }

    fun updateVibrateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(vibrateEnabled = enabled)
    }

    fun updateRepeatNotification(repeat: Boolean) {
        prefs.edit().putBoolean(KEY_REPEAT_NOTIFICATION, repeat).apply()
        _settings.value = _settings.value.copy(repeatNotification = repeat)
    }

    fun updateSoundType(soundType: String) {
        prefs.edit().putString(KEY_SOUND_TYPE, soundType).apply()
        _settings.value = _settings.value.copy(soundType = soundType)
    }

    fun updateLockscreenEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCKSCREEN_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isLockscreenEnabled = enabled)
    }

    fun updateAnimationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("animations_enabled", enabled).apply()
    }

    fun updateLockscreenThemeId(themeId: String) {
        prefs.edit().putString(KEY_LOCKSCREEN_THEME_ID, themeId).apply()
        _settings.value = _settings.value.copy(lockscreenThemeId = themeId)
    }

    fun updateLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _settings.value = _settings.value.copy(language = lang)
    }

    fun updateOemAutoStartAcknowledged(acknowledged: Boolean) {
        prefs.edit().putBoolean(KEY_OEM_AUTO_START_ACK, acknowledged).apply()
        _settings.value = _settings.value.copy(isOemAutoStartAcknowledged = acknowledged)
    }

    fun setLastTriggeredPercentage(percentage: Int) {
        prefs.edit().putInt(KEY_LAST_TRIGGERED_PCT, percentage).apply()
        _settings.value = _settings.value.copy(lastTriggeredPercentage = percentage)
    }

    fun resetLastTriggeredPercentage() {
        prefs.edit().putInt(KEY_LAST_TRIGGERED_PCT, -1).apply()
        _settings.value = _settings.value.copy(lastTriggeredPercentage = -1)
    }

    fun updateLowBatteryAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOW_BATTERY_ALERT_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isLowBatteryAlertEnabled = enabled)
    }

    fun updateLowBatteryPercentage(percentage: Int) {
        val validPct = percentage.coerceIn(5, 40)
        prefs.edit().putInt(KEY_LOW_BATTERY_PERCENTAGE, validPct).putInt(KEY_LAST_TRIGGERED_LOW_PCT, -1).apply()
        _settings.value = _settings.value.copy(lowBatteryPercentage = validPct, lastTriggeredLowPct = -1)
    }

    fun setLastTriggeredLowPercentage(percentage: Int) {
        prefs.edit().putInt(KEY_LAST_TRIGGERED_LOW_PCT, percentage).apply()
        _settings.value = _settings.value.copy(lastTriggeredLowPct = percentage)
    }

    fun resetLastTriggeredLowPercentage() {
        prefs.edit().putInt(KEY_LAST_TRIGGERED_LOW_PCT, -1).apply()
        _settings.value = _settings.value.copy(lastTriggeredLowPct = -1)
    }

    fun updateTemperatureAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TEMP_ALERT_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isTemperatureAlertEnabled = enabled)
    }

    fun updateTemperatureThreshold(threshold: Int) {
        val validThresh = threshold.coerceIn(35, 55)
        prefs.edit().putInt(KEY_TEMP_THRESHOLD, validThresh).putFloat(KEY_LAST_TRIGGERED_TEMP, -1f).apply()
        _settings.value = _settings.value.copy(temperatureThreshold = validThresh, lastTriggeredTemp = -1f)
    }

    fun setLastTriggeredTemperature(temp: Float) {
        prefs.edit().putFloat(KEY_LAST_TRIGGERED_TEMP, temp).apply()
        _settings.value = _settings.value.copy(lastTriggeredTemp = temp)
    }

    fun resetLastTriggeredTemperature() {
        prefs.edit().putFloat(KEY_LAST_TRIGGERED_TEMP, -1f).apply()
        _settings.value = _settings.value.copy(lastTriggeredTemp = -1f)
    }

    fun updateVoiceAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_ALERT_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isVoiceAlertEnabled = enabled)
    }

    companion object {
        private const val KEY_TARGET_PERCENTAGE = "target_percentage"
        private const val KEY_ALERT_ENABLED = "alert_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATE_ENABLED = "vibrate_enabled"
        private const val KEY_REPEAT_NOTIFICATION = "repeat_notification"
        private const val KEY_LAST_TRIGGERED_PCT = "last_triggered_pct"
        private const val KEY_SOUND_TYPE = "sound_type"
        private const val KEY_LOCKSCREEN_ENABLED = "lockscreen_enabled"
        private const val KEY_LOCKSCREEN_THEME_ID = "lockscreen_theme_id"
        private const val KEY_ALWAYS_ON_MONITOR = "always_on_monitor"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_OEM_AUTO_START_ACK = "oem_autostart_ack"

        private const val KEY_LOW_BATTERY_ALERT_ENABLED = "low_battery_alert_enabled"
        private const val KEY_LOW_BATTERY_PERCENTAGE = "low_battery_percentage"
        private const val KEY_LAST_TRIGGERED_LOW_PCT = "last_triggered_low_pct"
        private const val KEY_TEMP_ALERT_ENABLED = "temp_alert_enabled"
        private const val KEY_TEMP_THRESHOLD = "temp_threshold"
        private const val KEY_LAST_TRIGGERED_TEMP = "last_triggered_temp"
        private const val KEY_VOICE_ALERT_ENABLED = "voice_alert_enabled"

        @Volatile
        private var instance: PreferencesRepository? = null

        fun getInstance(context: Context): PreferencesRepository {
            return instance ?: synchronized(this) {
                instance ?: PreferencesRepository(context).also { instance = it }
            }
        }

        operator fun invoke(context: Context): PreferencesRepository = getInstance(context)
    }
}
