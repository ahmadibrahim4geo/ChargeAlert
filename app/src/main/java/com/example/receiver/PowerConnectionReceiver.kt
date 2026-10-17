package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.ChargingSessionStore
import com.example.util.MonitoringController
import com.example.util.NotificationHelper

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_DISCONNECTED -> {
                ChargingSessionStore(context).observeConnection(false)
                NotificationHelper.cancelAlertNotification(context)
                context.sendBroadcast(Intent(NotificationHelper.ACTION_DISMISS_SCREEN).setPackage(context.packageName))
                MonitoringController.sync(context)
            }
            Intent.ACTION_POWER_CONNECTED -> MonitoringController.sync(context, Intent.ACTION_POWER_CONNECTED)
            Intent.ACTION_BOOT_COMPLETED -> {
                ChargingSessionStore(context).write(com.example.model.ChargingSession())
                MonitoringController.sync(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> MonitoringController.sync(context)
        }
    }
}
