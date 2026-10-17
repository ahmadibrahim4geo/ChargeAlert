package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.data.ChargingSessionStore
import com.example.data.PreferencesRepository
import com.example.service.BatteryMonitoringService
import com.example.worker.ChargingWorkScheduler

object MonitoringController {
    fun sync(context: Context, action: String = BatteryMonitoringService.ACTION_REFRESH) {
        val settings = PreferencesRepository(context).loadSettings()
        val store = ChargingSessionStore(context)
        val battery = BatteryReader.read(context, settings.targetPercentage)
        val connected = battery?.isPowerConnected == true
        if (!connected && battery != null) store.observeConnection(false)
        if (settings.isAlertEnabled) ChargingWorkScheduler.schedule(context) else ChargingWorkScheduler.cancel(context)
        if (!settings.isAlertEnabled || store.read().monitorPaused || (!connected && !settings.isAlwaysOnMonitorEnabled)) {
            context.stopService(Intent(context, BatteryMonitoringService::class.java))
            NotificationHelper.cancelAlertNotification(context)
            return
        }
        try {
            ContextCompat.startForegroundService(context, Intent(context, BatteryMonitoringService::class.java).setAction(action))
        } catch (_: IllegalStateException) {
            NotificationHelper.showResumeMonitoringNotification(context)
        } catch (_: SecurityException) {
            NotificationHelper.showResumeMonitoringNotification(context)
        }
    }
}
