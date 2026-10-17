package com.example

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChargingSessionStore
import com.example.data.PreferencesRepository
import com.example.receiver.StopAlarmReceiver
import com.example.service.BatteryMonitoringService
import com.example.util.BatteryReader
import com.example.util.NotificationHelper
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ChargingSessionIntegrationTest {
    private lateinit var context: Context
    private lateinit var repo: PreferencesRepository
    private var controller: ServiceController<BatteryMonitoringService>? = null
    private val nm get() = shadowOf(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("charging_session", 0).edit().clear().commit()
        context.getSharedPreferences("charge_alert_prefs", 0).edit().clear().commit()
        PreferencesRepository::class.java.getDeclaredField("instance").apply { isAccessible = true }.set(null, null)
        repo = PreferencesRepository(context)
        repo.updateSoundEnabled(false)
        repo.updateVibrateEnabled(false)
    }

    private fun battery(level: Int, plugged: Boolean = true, status: Int = BatteryManager.BATTERY_STATUS_CHARGING) =
        Intent(Intent.ACTION_BATTERY_CHANGED).putExtra(BatteryManager.EXTRA_LEVEL, level)
            .putExtra(BatteryManager.EXTRA_SCALE, 100)
            .putExtra(BatteryManager.EXTRA_PLUGGED, if (plugged) BatteryManager.BATTERY_PLUGGED_AC else 0)
            .putExtra(BatteryManager.EXTRA_STATUS, status)
            .putExtra(BatteryManager.EXTRA_VOLTAGE, 4200)
            .putExtra(BatteryManager.EXTRA_TEMPERATURE, 320)

    @Suppress("DEPRECATION")
    private fun sendBattery(level: Int) {
        context.sendStickyBroadcast(battery(level))
        shadowOf(android.os.Looper.getMainLooper()).idle()
    }

    private fun start(action: String = Intent.ACTION_POWER_CONNECTED) {
        controller = Robolectric.buildService(BatteryMonitoringService::class.java, Intent().setAction(action)).create()
            .startCommand(0, 0)
        shadowOf(android.os.Looper.getMainLooper()).idle()
    }

    @After fun cleanup() {
        controller?.destroy()
        NotificationHelper.stopAlarmSound(context)
    }

    @Test fun stopAndFurtherBatteryUpdatesDoNotRecreateAlertEvenWithRepeat() {
        repo.updateRepeatNotification(true)
        sendBattery(79)
        start()
        assertNull(nm.getNotification(NotificationHelper.NOTIFICATION_ID_ALERT))
        sendBattery(80)
        assertNotNull(nm.getNotification(NotificationHelper.NOTIFICATION_ID_ALERT))
        StopAlarmReceiver().onReceive(context, Intent().setAction(StopAlarmReceiver.ACTION_STOP_ALARM))
        shadowOf(android.os.Looper.getMainLooper()).idle()
        sendBattery(81)
        assertNull(nm.getNotification(NotificationHelper.NOTIFICATION_ID_ALERT))
        assertFalse(NotificationHelper.isAlarmActive())
        val monitor = nm.getNotification(NotificationHelper.NOTIFICATION_ID_SERVICE)
        assertFalse(monitor.extras.getCharSequence("android.title").toString().contains("نشط"))
    }

    @Test fun reconnectResetsDismissalButRestartDoesNot() {
        val store = ChargingSessionStore(context)
        store.observeConnection(true)
        store.dismissTarget(80)
        assertEquals(80, ChargingSessionStore(context).read().dismissedTarget)
        store.observeConnection(false)
        store.observeConnection(true)
        assertEquals(-1, store.read().dismissedTarget)
        assertFalse(store.read().screenPresented)
    }

    @Test fun blockedTargetNotificationDoesNotConsumeTheTarget() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(android.app.NotificationChannel(NotificationHelper.CHANNEL_ALERT_ALARM_ID,
            "Blocked target", NotificationManager.IMPORTANCE_NONE))
        sendBattery(80)
        start()
        assertNull(nm.getNotification(NotificationHelper.NOTIFICATION_ID_ALERT))
        assertEquals(-1, ChargingSessionStore(context).read().lastAlertedTarget)
        assertFalse(NotificationHelper.isAlarmActive())
    }

    @Test fun connectingAtTheTargetPostsOnlyOneFullscreenNotification() {
        sendBattery(80)
        start()
        assertNotNull(nm.getNotification(NotificationHelper.NOTIFICATION_ID_ALERT))
        assertNull(nm.getNotification(NotificationHelper.NOTIFICATION_ID_PLUGGED_IN))
    }

    @Test fun recoveryAndRepeatedStartCommandsDoNotReopenChargingDisplay() {
        sendBattery(60)
        start(BatteryMonitoringService.ACTION_REFRESH)
        assertNull(nm.getNotification(NotificationHelper.NOTIFICATION_ID_PLUGGED_IN))
        controller!!.get().onStartCommand(Intent().setAction(Intent.ACTION_POWER_CONNECTED), 0, 1)
        assertNotNull(nm.getNotification(NotificationHelper.NOTIFICATION_ID_PLUGGED_IN))
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(NotificationHelper.NOTIFICATION_ID_PLUGGED_IN)
        controller!!.get().onStartCommand(Intent().setAction(Intent.ACTION_POWER_CONNECTED), 0, 2)
        sendBattery(61)
        assertNull(nm.getNotification(NotificationHelper.NOTIFICATION_ID_PLUGGED_IN))
    }

    @Test fun swipingMonitorSuppressesLaterProgressUpdates() {
        sendBattery(60)
        start()
        val original = nm.getNotification(NotificationHelper.NOTIFICATION_ID_SERVICE)
        StopAlarmReceiver().onReceive(context, Intent().setAction(StopAlarmReceiver.ACTION_DISMISS_MONITOR_NOTIFICATION))
        sendBattery(61)
        assertSame(original, nm.getNotification(NotificationHelper.NOTIFICATION_ID_SERVICE))
        assertTrue(ChargingSessionStore(context).read().notificationDismissed)
    }

    @Test fun snoozeIsPersistedAcrossStoreRecreation() {
        val store = ChargingSessionStore(context)
        store.observeConnection(true)
        store.snooze(80, 123456789)
        assertEquals(123456789L, ChargingSessionStore(context).read().snoozedUntil)
    }

    @Test fun openingTheChargingViewDoesNotCancelATestAlarmWhileUnplugged() {
        context.sendStickyBroadcast(battery(60, plugged = false, status = BatteryManager.BATTERY_STATUS_DISCHARGING))
        NotificationHelper.showTargetReachedNotification(context, 60, 80,
            soundEnabled = false, vibrateEnabled = false, isTest = true)
        val model = com.example.ui.MainViewModel(context as Application)
        assertEquals(60, model.batteryInfo.value.level)
        assertTrue(NotificationHelper.isAlarmActive())
        assertEquals(-1, ChargingSessionStore(context).read().dismissedTarget)
        androidx.lifecycle.ViewModelStore().apply { put("test", model); clear() }
    }

    @Test fun pluggedButPausedAndMissingTelemetryAreNotFabricated() {
        val reading = BatteryReader.fromIntent(context,
            battery(75, status = BatteryManager.BATTERY_STATUS_NOT_CHARGING).removeExtraReturning(BatteryManager.EXTRA_TEMPERATURE), 80)!!
        assertTrue(reading.isPowerConnected)
        assertFalse(reading.isCharging)
        assertEquals("Charging Paused", reading.chargingSpeedCategory)
        assertTrue(reading.temperature.isNaN())
        assertEquals(-1, reading.estimatedMinutesRemaining)
        assertEquals(0f, reading.wattage, 0f)
    }

    private fun Intent.removeExtraReturning(key: String): Intent = apply { removeExtra(key) }
}
