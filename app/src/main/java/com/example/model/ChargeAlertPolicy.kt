package com.example.model

/** Decisions depend on the OS reading and the current cable session, never on UI lifetime. */
object ChargeAlertPolicy {
    const val REPEAT_INTERVAL_MS = 5 * 60 * 1000L

    fun shouldAlert(level: Int, connected: Boolean, settings: AlertSettings,
                    session: ChargingSession, now: Long): Boolean {
        if (!settings.isAlertEnabled || !connected || level !in 0..100 ||
            level < settings.targetPercentage || session.monitorPaused ||
            session.dismissedTarget == settings.targetPercentage) return false
        if (session.snoozedTarget == settings.targetPercentage) {
            return now >= session.snoozedUntil
        }
        return session.lastAlertedTarget != settings.targetPercentage ||
            (settings.repeatNotification && now - session.lastAlertAt >= REPEAT_INTERVAL_MS)
    }
}

data class ChargingSession(
    val connected: Boolean = false,
    val screenPresented: Boolean = false,
    val dismissedTarget: Int = -1,
    val lastAlertedTarget: Int = -1,
    val lastAlertAt: Long = 0,
    val snoozedTarget: Int = -1,
    val snoozedUntil: Long = 0,
    val notificationDismissed: Boolean = false,
    val monitorPaused: Boolean = false,
    val temperatureAlerted: Boolean = false,
    val lowBatteryAlerted: Boolean = false
)
