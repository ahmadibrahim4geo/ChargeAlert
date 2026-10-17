package com.example

import android.app.Application
import android.content.IntentFilter
import android.os.Build
import com.example.receiver.PowerConnectionReceiver
import com.example.util.NotificationHelper

/**
 * Custom Application class that:
 *
 * 1. Creates notification channels on startup (required for Android 8+)
 * 2. Dynamically registers the PowerConnectionReceiver
 *
 * WHY DYNAMIC REGISTRATION IS REQUIRED:
 * Since Android 8.0 (API 26), manifest-declared BroadcastReceivers for most
 * implicit broadcasts (like ACTION_POWER_CONNECTED / ACTION_POWER_DISCONNECTED)
 * are NO LONGER DELIVERED by the system. The only reliable way is to register
 * them dynamically from Application.onCreate() so they live as long as the
 * process does.
 *
 * BOOT_COMPLETED is exempt from this restriction and CAN still be received
 * via manifest declaration, so we keep it in the manifest as a safety net.
 */
class ChargeAlertApplication : Application() {

    private var powerReceiver: PowerConnectionReceiver? = null

    override fun onCreate() {
        super.onCreate()

        // Create all notification channels early so they're ready when needed
        NotificationHelper.createNotificationChannels(this)

        // Register PowerConnectionReceiver dynamically for active process monitoring
        registerPowerReceiver()

        // Remove the old self-rescheduling job and keep one inexact recovery worker.
        com.example.worker.ChargingWorkScheduler.migrateLegacyWork(this)
        if (com.example.data.PreferencesRepository(this).loadSettings().isAlertEnabled) {
            com.example.worker.ChargingWorkScheduler.schedule(this)
        }
    }

    private fun registerPowerReceiver() {
        if (powerReceiver != null) return

        powerReceiver = PowerConnectionReceiver()
        val filter = IntentFilter().apply {
            addAction(android.content.Intent.ACTION_POWER_CONNECTED)
            addAction(android.content.Intent.ACTION_POWER_DISCONNECTED)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(powerReceiver, filter, RECEIVER_EXPORTED)
            } else {
                registerReceiver(powerReceiver, filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
