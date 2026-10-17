package com.example.data

import android.content.Context
import com.example.model.ChargingSession

/** Persist user decisions across activity, service and process recreation. Excluded from backup. */
class ChargingSessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("charging_session", Context.MODE_PRIVATE)

    fun read() = ChargingSession(
        connected = prefs.getBoolean("connected", false),
        screenPresented = prefs.getBoolean("screen", false),
        dismissedTarget = prefs.getInt("dismissed", -1),
        lastAlertedTarget = prefs.getInt("target", -1),
        lastAlertAt = prefs.getLong("alert_at", 0),
        snoozedTarget = prefs.getInt("snoozed_target", -1),
        snoozedUntil = prefs.getLong("snoozed_until", 0),
        notificationDismissed = prefs.getBoolean("notification_dismissed", false),
        monitorPaused = prefs.getBoolean("paused", false),
        temperatureAlerted = prefs.getBoolean("temperature", false),
        lowBatteryAlerted = prefs.getBoolean("low", false)
    )

    fun write(state: ChargingSession) {
        prefs.edit().putBoolean("connected", state.connected)
            .putBoolean("screen", state.screenPresented).putInt("dismissed", state.dismissedTarget)
            .putInt("target", state.lastAlertedTarget).putLong("alert_at", state.lastAlertAt)
            .putInt("snoozed_target", state.snoozedTarget).putLong("snoozed_until", state.snoozedUntil)
            .putBoolean("notification_dismissed", state.notificationDismissed)
            .putBoolean("paused", state.monitorPaused).putBoolean("temperature", state.temperatureAlerted)
            .putBoolean("low", state.lowBatteryAlerted).apply()
    }

    fun observeConnection(connected: Boolean): ChargingSession {
        val old = read()
        if (old.connected == connected) return old
        return ChargingSession(connected = connected, lowBatteryAlerted = if (connected) false else old.lowBatteryAlerted)
            .also(::write)
    }

    fun dismissTarget(target: Int) = write(read().copy(dismissedTarget = target, snoozedTarget = -1, snoozedUntil = 0))
    fun snooze(target: Int, until: Long) = write(read().copy(snoozedTarget = target, snoozedUntil = until))
}
