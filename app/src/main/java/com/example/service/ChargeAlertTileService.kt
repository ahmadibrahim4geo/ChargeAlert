package com.example.service

import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.BatteryManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.data.PreferencesRepository
import com.example.receiver.StopAlarmReceiver
import com.example.util.NotificationHelper

/**
 * Quick Settings Tile in Android's pull-down Control Center / Quick Settings shade.
 * Allows user to instantly silence any active alarm or check charge guard status with one tap.
 */
@RequiresApi(Build.VERSION_CODES.N)
class ChargeAlertTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val repo = PreferencesRepository.getInstance(this)
        val settings = repo.loadSettings()
        val isAlarmActive = NotificationHelper.isAlarmSounding()

        if (isAlarmActive) {
            // Stop alarm immediately from the Quick Settings tile
            val stopIntent = Intent(this, StopAlarmReceiver::class.java).apply {
                action = StopAlarmReceiver.ACTION_STOP_ALARM
            }
            sendBroadcast(stopIntent)
            updateTileState()
        } else {
            // Toggle alert guard enabled state
            val newEnabled = !settings.isAlertEnabled
            repo.updateAlertEnabled(newEnabled)
            if (newEnabled) {
                val store = com.example.data.ChargingSessionStore(this)
                store.write(store.read().copy(monitorPaused = false, notificationDismissed = false))
            }
            com.example.util.MonitoringController.sync(this)
            updateTileState()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val repo = PreferencesRepository.getInstance(this)
        val settings = repo.loadSettings()
        val isEn = settings.language == "en"
        val isAlarmActive = NotificationHelper.isAlarmSounding()

        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val rawLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val currentLevel = if (rawLevel != -1 && scale > 0) (rawLevel * 100) / scale else -1

        tile.icon = Icon.createWithResource(this, com.example.R.drawable.ic_stat_charge_alert)

        if (isAlarmActive) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = if (isEn) "Stop Alarm 🔕" else "إيقاف التنبيه 🔕"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (isEn) "$currentLevel% • Ringing!" else "$currentLevel% • يرن الآن!"
            }
        } else if (settings.isAlertEnabled) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = if (isEn) "Charge Alert" else "حارس الشحن"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (isEn) "$currentLevel% → ${settings.targetPercentage}%" else "$currentLevel% ← ${settings.targetPercentage}%"
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = if (isEn) "Charge Alert" else "حارس الشحن"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (isEn) "Disabled" else "معطّل"
            }
        }

        tile.updateTile()
    }
}
