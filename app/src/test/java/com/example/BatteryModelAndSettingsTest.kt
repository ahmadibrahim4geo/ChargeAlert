package com.example

import com.example.model.AlertSettings
import com.example.model.ChargingSession
import com.example.model.ChargeAlertPolicy
import com.example.util.BatteryReader
import org.junit.Assert.*
import org.junit.Test

class BatteryModelAndSettingsTest {
    private val settings = AlertSettings()
    private fun should(level: Int, state: ChargingSession = ChargingSession(connected = true),
        config: AlertSettings = settings, now: Long = 1_000_000) =
        ChargeAlertPolicy.shouldAlert(level, true, config, state, now)

    @Test fun targetUsesActualReading() {
        assertFalse(should(79))
        assertTrue(should(80))
        assertTrue(should(81))
        assertFalse(should(-1))
        assertFalse(should(101))
    }
    @Test fun fullTargetDoesNotFireAt99() {
        assertFalse(should(99, config = settings.copy(targetPercentage = 100)))
        assertTrue(should(100, config = settings.copy(targetPercentage = 100)))
    }
    @Test fun disconnectedNeverAlerts() {
        assertFalse(ChargeAlertPolicy.shouldAlert(90, false, settings, ChargingSession(), 1000))
    }
    @Test fun disabledAndPausedNeverAlert() {
        assertFalse(should(80, config = settings.copy(isAlertEnabled = false)))
        assertFalse(should(80, ChargingSession(monitorPaused = true)))
    }
    @Test fun batteryFluctuationAndServiceRestartDoNotRearmTarget() {
        val state = ChargingSession(lastAlertedTarget = 80)
        assertFalse(should(80, state))
        assertFalse(should(79, state))
        assertFalse(should(81, state.copy()))
    }
    @Test fun dismissalWinsEvenWithRepeatEnabled() {
        assertFalse(should(99, ChargingSession(dismissedTarget = 80), settings.copy(repeatNotification = true),
            now = Long.MAX_VALUE))
    }
    @Test fun closingChargingScreenDoesNotSuppressTarget() {
        assertTrue(should(80, ChargingSession(screenPresented = true)))
    }
    @Test fun repeatHasAnActualFiveMinuteInterval() {
        val state = ChargingSession(lastAlertedTarget = 80, lastAlertAt = 1_000_000)
        val repeat = settings.copy(repeatNotification = true)
        assertFalse(should(90, state, repeat, 1_299_999))
        assertTrue(should(90, state, repeat, 1_300_000))
    }
    @Test fun snoozeSurvivesRecreationAndWorksWithoutRepeatEnabled() {
        val state = ChargingSession(lastAlertedTarget = 80, snoozedTarget = 80, snoozedUntil = 1_300_000)
        assertFalse(should(90, state.copy(), now = 1_299_999))
        assertTrue(should(90, state.copy(), now = 1_300_000))
    }
    @Test fun changingTargetArmsNewThreshold() {
        val state = ChargingSession(lastAlertedTarget = 80, dismissedTarget = 80)
        assertFalse(should(89, state, settings.copy(targetPercentage = 90)))
        assertTrue(should(90, state, settings.copy(targetPercentage = 90)))
    }
    @Test fun validatesScaleAndRejectsInventedLevels() {
        assertEquals(80, BatteryReader.percentage(160, 200))
        assertEquals(99, BatteryReader.percentage(199, 200))
        assertEquals(0, BatteryReader.percentage(0, 100))
        assertEquals(100, BatteryReader.percentage(100, 100))
        assertNull(BatteryReader.percentage(-1, 100))
        assertNull(BatteryReader.percentage(101, 100))
        assertNull(BatteryReader.percentage(80, 0))
        assertEquals(100, BatteryReader.percentage(Int.MAX_VALUE, Int.MAX_VALUE))
    }
    @Test fun currentAlwaysUsesMicroamperesAndRejectsUnsupportedSentinel() {
        assertEquals(2500, BatteryReader.currentMilliamps(2_500_000))
        assertEquals(-500, BatteryReader.currentMilliamps(-500_000))
        assertEquals(9, BatteryReader.currentMilliamps(9_000))
        assertEquals(0, BatteryReader.currentMilliamps(0))
        assertNull(BatteryReader.currentMilliamps(Int.MIN_VALUE))
    }
}
