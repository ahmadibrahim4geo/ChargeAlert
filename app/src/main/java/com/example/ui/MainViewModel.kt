package com.example.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.example.data.PreferencesRepository
import com.example.model.AlertSettings
import com.example.model.BatteryInfo
import com.example.service.BatteryMonitoringService
import com.example.util.NotificationHelper
import android.app.NotificationManager
import androidx.core.content.ContextCompat
import com.example.util.PowerManagementUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PermissionState(
    val hasNotificationPermission: Boolean = true,
    val isIgnoringBatteryOptimizations: Boolean = true,
    val canUseFullScreenIntent: Boolean = true,
    val isOemAutoStartAcknowledged: Boolean = false,
    val hasOemAutoStartIntent: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository.getInstance(application)

    val alertSettings: StateFlow<AlertSettings> = preferencesRepository.settings

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null

    init {
        registerBatteryReceiver()
        refreshPermissions()
    }

    private fun registerBatteryReceiver() {
        if (batteryReceiver != null) return
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) return
                updateBatteryInfoFromIntent(intent)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            val app = getApplication<Application>()
            // ACTION_BATTERY_CHANGED is a sticky broadcast sent only by the OS.
            // RECEIVER_NOT_EXPORTED is correct here — no external app should send this.
            val initialIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                app.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                app.registerReceiver(batteryReceiver, filter)
            }
            if (initialIntent != null) {
                updateBatteryInfoFromIntent(initialIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateBatteryInfoFromIntent(intent: Intent) {
        val newInfo = com.example.util.BatteryReader.fromIntent(getApplication(), intent,
            alertSettings.value.targetPercentage) ?: return
        _batteryInfo.value = newInfo
    }
    // ─────────────────────────────────────────────────────────────────────────
    // Settings mutators
    // ─────────────────────────────────────────────────────────────────────────

    fun setTargetPercentage(target: Int) {
        if (target == alertSettings.value.targetPercentage) return
        NotificationHelper.cancelAlertNotification(getApplication())
        val store = com.example.data.ChargingSessionStore(getApplication())
        store.write(store.read().copy(dismissedTarget = -1, lastAlertedTarget = -1,
            snoozedTarget = -1, snoozedUntil = 0))
        preferencesRepository.updateTargetPercentage(target)
        com.example.util.BatteryReader.read(getApplication(), target)?.let { _batteryInfo.value = it }
        syncServiceState()
    }

    fun setAlertEnabled(enabled: Boolean) {
        preferencesRepository.updateAlertEnabled(enabled)
        if (enabled) {
            val store = com.example.data.ChargingSessionStore(getApplication())
            store.write(store.read().copy(monitorPaused = false, notificationDismissed = false))
        }
        syncServiceState()
    }

    fun setSoundEnabled(enabled: Boolean)             = preferencesRepository.updateSoundEnabled(enabled)
    fun setSoundType(soundType: String)               = preferencesRepository.updateSoundType(soundType)
    fun setVibrateEnabled(enabled: Boolean)           = preferencesRepository.updateVibrateEnabled(enabled)
    fun setRepeatNotification(repeat: Boolean)        = preferencesRepository.updateRepeatNotification(repeat)
    fun setLockscreenEnabled(enabled: Boolean)        = preferencesRepository.updateLockscreenEnabled(enabled)
    fun setLockscreenThemeId(themeId: String)         = preferencesRepository.updateLockscreenThemeId(themeId)
    fun setLanguage(lang: String)                     = preferencesRepository.updateLanguage(lang)
    fun setOemAutoStartAcknowledged(ack: Boolean)     = preferencesRepository.updateOemAutoStartAcknowledged(ack)

    fun setLowBatteryAlertEnabled(enabled: Boolean)   = preferencesRepository.updateLowBatteryAlertEnabled(enabled)
    fun setLowBatteryPercentage(percentage: Int)     = preferencesRepository.updateLowBatteryPercentage(percentage)
    fun setTemperatureAlertEnabled(enabled: Boolean) = preferencesRepository.updateTemperatureAlertEnabled(enabled)
    fun setTemperatureThreshold(threshold: Int)      = preferencesRepository.updateTemperatureThreshold(threshold)
    fun setVoiceAlertEnabled(enabled: Boolean)        = preferencesRepository.updateVoiceAlertEnabled(enabled)

    // ─────────────────────────────────────────────────────────────────────────
    // Sound preview
    // ─────────────────────────────────────────────────────────────────────────

    fun previewSound(soundType: String) {
        try {
            stopSoundPreview()
            NotificationHelper.playAlarmSoundBypassingSilent(getApplication(), soundType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSoundPreview() {
        try { NotificationHelper.stopAlarmSound(getApplication()) } catch (_: Exception) {}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test notification (for the test button in settings)
    // ─────────────────────────────────────────────────────────────────────────

    fun testNotification() {
        val settings     = alertSettings.value
        val currentLevel = batteryInfo.value.level
        NotificationHelper.showTargetReachedNotification(
            context        = getApplication(),
            currentLevel   = currentLevel,
            targetLevel    = settings.targetPercentage,
            soundEnabled   = settings.soundEnabled,
            vibrateEnabled = settings.vibrateEnabled,
            soundType      = settings.soundType,
            isTest         = true
        )
    }

    fun updateAlwaysOnMonitorEnabled(enabled: Boolean) {
        preferencesRepository.updateAlwaysOnMonitorEnabled(enabled)
        syncServiceState()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Service lifecycle sync
    // ─────────────────────────────────────────────────────────────────────────

    fun syncServiceState() {
        refreshPermissions()
        com.example.util.MonitoringController.sync(getApplication())
    }
    fun openStandbyChannelSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, NotificationHelper.CHANNEL_STANDBY_ID)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                val appIntent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try { context.startActivity(appIntent) } catch (_: Exception) {}
            }
        } else {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try { context.startActivity(intent) } catch (_: Exception) {}
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Permission Management & State Refreshing
    // ─────────────────────────────────────────────────────────────────────────

    fun refreshPermissions() {
        val context = getApplication<Application>()

        val hasNotif = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled() && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val isIgnoringBat = isIgnoringBatteryOptimizations(context)

        val canFsi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.canUseFullScreenIntent() ?: true
        } else {
            true
        }

        val oemIntent = PowerManagementUtil.getOemAutoStartIntent(context)
        val isOemAck = alertSettings.value.isOemAutoStartAcknowledged

        _permissionState.value = PermissionState(
            hasNotificationPermission = hasNotif,
            isIgnoringBatteryOptimizations = isIgnoringBat,
            canUseFullScreenIntent = canFsi,
            isOemAutoStartAcknowledged = isOemAck,
            hasOemAutoStartIntent = (oemIntent != null)
        )
    }

    fun acknowledgeOemAutoStart() {
        preferencesRepository.updateOemAutoStartAcknowledged(true)
        refreshPermissions()
    }

    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Battery optimization helpers
    // ─────────────────────────────────────────────────────────────────────────

    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data  = android.net.Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val fallback = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallback)
                } catch (_: Exception) {}
            }
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } else {
            true // API < 23: battery optimizations not applicable
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cleanup
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        batteryReceiver?.let {
            try { getApplication<Application>().unregisterReceiver(it) } catch (_: Exception) {}
        }
        batteryReceiver = null
    }
}
