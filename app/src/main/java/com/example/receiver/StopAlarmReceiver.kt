package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.ChargingSessionStore
import com.example.data.PreferencesRepository
import com.example.service.BatteryMonitoringService
import com.example.ui.ChargingAlarmActivity
import com.example.util.NotificationHelper

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessions = ChargingSessionStore(context)
        when (intent.action) {
            ACTION_STOP_ALARM -> {
                if (!intent.getBooleanExtra(ChargingAlarmActivity.EXTRA_IS_TEST, false)) {
                    sessions.dismissTarget(PreferencesRepository(context).loadSettings().targetPercentage)
                }
                NotificationHelper.cancelAlertNotification(context)
                context.sendBroadcast(Intent(NotificationHelper.ACTION_DISMISS_SCREEN).setPackage(context.packageName))
                BatteryMonitoringService.refresh(context)
            }
            ACTION_DISMISS_MONITOR_NOTIFICATION -> sessions.write(sessions.read().copy(notificationDismissed = true))
            ACTION_PAUSE_MONITOR -> {
                sessions.write(sessions.read().copy(monitorPaused = true))
                NotificationHelper.cancelAlertNotification(context)
                context.stopService(Intent(context, BatteryMonitoringService::class.java))
                context.sendBroadcast(Intent(NotificationHelper.ACTION_DISMISS_SCREEN).setPackage(context.packageName))
            }
        }
    }
    companion object {
        const val ACTION_STOP_ALARM = "com.example.ACTION_STOP_ALARM"
        const val ACTION_DISMISS_MONITOR_NOTIFICATION = "com.example.DISMISS_MONITOR_NOTIFICATION"
        const val ACTION_PAUSE_MONITOR = "com.example.PAUSE_MONITOR"
    }
}
