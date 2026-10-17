package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.ChargingSessionStore
import com.example.data.PreferencesRepository
import com.example.model.BatteryInfo
import com.example.model.ChargeAlertPolicy
import com.example.util.BatteryReader
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Sole owner of charging transitions, alarms and foreground notifications. */
class BatteryMonitoringService : Service() {
    private lateinit var repo: PreferencesRepository
    private lateinit var sessions: ChargingSessionStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private val evaluateLater = Runnable { BatteryReader.read(this, repo.loadSettings().targetPercentage)?.let { evaluate(it) } }
    private var receiver: BroadcastReceiver? = null
    private var lastNotificationKey: String? = null
    private var foregroundStarted = false

    override fun onCreate() {
        super.onCreate()
        repo = PreferencesRepository(this)
        sessions = ChargingSessionStore(this)
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val settings = repo.loadSettings()
        val battery = BatteryReader.read(this, settings.targetPercentage)
        if (intent?.action == ACTION_RESUME) {
            sessions.write(sessions.read().copy(monitorPaused = false, notificationDismissed = false))
        }
        val session = sessions.read()
        // Promote before registering the sticky receiver: its callback may stop the service.
        if (!foregroundStarted) {
            publish(if (battery?.isPowerConnected == true)
                NotificationHelper.buildServiceNotification(this, battery.level, settings.targetPercentage, true)
            else NotificationHelper.buildStandbyNotification(this))
        }
        if (!settings.isAlertEnabled || session.monitorPaused ||
            (battery?.isPowerConnected != true && !settings.isAlwaysOnMonitorEnabled)) {
            stopMonitoring()
            return START_NOT_STICKY
        }
        if (intent?.action == Intent.ACTION_POWER_CONNECTED && !session.screenPresented) {
            val connectedSession = sessions.observeConnection(true)
            sessions.write(connectedSession.copy(screenPresented = true))
            if (settings.isLockscreenEnabled && battery != null && battery.level < settings.targetPercentage)
                NotificationHelper.showPluggedInLockscreenNotification(this)
        }
        if (receiver == null) {
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                        BatteryReader.fromIntent(this@BatteryMonitoringService, intent, repo.loadSettings().targetPercentage)?.let { evaluate(it, !isInitialStickyBroadcast) }
                    }
                }
            }
            ContextCompat.registerReceiver(this, receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED)
            scope.launch { repo.settings.collect { BatteryReader.read(this@BatteryMonitoringService, it.targetPercentage)?.let { evaluate(it) } } }
        }
        battery?.let { evaluate(it) }
        return START_STICKY
    }

    private fun evaluate(battery: BatteryInfo, allowConnectionScreen: Boolean = false) {
        val settings = repo.loadSettings()
        val oldSession = sessions.read()
        var session = sessions.observeConnection(battery.isPowerConnected)
        handler.removeCallbacks(evaluateLater)
        if (!settings.isAlertEnabled || session.monitorPaused) {
            stopMonitoring()
            return
        }
        if (!battery.isPowerConnected) {
            NotificationHelper.cancelAlertNotification(this)
            sendBroadcast(Intent(NotificationHelper.ACTION_DISMISS_SCREEN).setPackage(packageName))
            if (!settings.isAlwaysOnMonitorEnabled) {
                stopMonitoring()
                return
            }
        } else if (allowConnectionScreen && !oldSession.connected && !session.screenPresented) {
            sessions.write(session.copy(screenPresented = true))
            if (settings.isLockscreenEnabled && battery.level < settings.targetPercentage)
                NotificationHelper.showPluggedInLockscreenNotification(this)
        }
        val now = System.currentTimeMillis()
        session = sessions.read()
        if (ChargeAlertPolicy.shouldAlert(battery.level, battery.isPowerConnected, settings, session, now)) {
            if (NotificationHelper.showTargetReachedNotification(this, battery.level, settings.targetPercentage,
                    settings.soundEnabled, settings.vibrateEnabled, settings.soundType)) {
                sessions.write(session.copy(lastAlertedTarget = settings.targetPercentage, lastAlertAt = now,
                    snoozedTarget = -1, snoozedUntil = 0))
            }
        }
        session = sessions.read()
        if (settings.isTemperatureAlertEnabled && battery.isPowerConnected && battery.temperature.isFinite()) {
            if (battery.temperature >= settings.temperatureThreshold && !session.temperatureAlerted) {
                sessions.write(session.copy(temperatureAlerted = true))
                NotificationHelper.showTemperatureAlertNotification(this, battery.temperature, settings.temperatureThreshold,
                    settings.soundEnabled, settings.vibrateEnabled)
            } else if (battery.temperature < settings.temperatureThreshold - 2 && session.temperatureAlerted) {
                sessions.write(session.copy(temperatureAlerted = false))
            }
        }
        session = sessions.read()
        if (!battery.isPowerConnected && settings.isAlwaysOnMonitorEnabled && settings.isLowBatteryAlertEnabled) {
            if (battery.level <= settings.lowBatteryPercentage && !session.lowBatteryAlerted) {
                sessions.write(session.copy(lowBatteryAlerted = true))
                NotificationHelper.showLowBatteryNotification(this, battery.level, settings.lowBatteryPercentage,
                    settings.soundEnabled, settings.vibrateEnabled)
            } else if (battery.level > settings.lowBatteryPercentage + 2 && session.lowBatteryAlerted) {
                sessions.write(session.copy(lowBatteryAlerted = false))
            }
        }
        val alarmActive = NotificationHelper.isAlarmActive()
        val key = "${battery.level}:${battery.isPowerConnected}:${settings.targetPercentage}:$alarmActive:${settings.language}"
        if (!sessions.read().notificationDismissed && key != lastNotificationKey) {
            publish(if (battery.isPowerConnected) NotificationHelper.buildServiceNotification(this, battery.level,
                settings.targetPercentage, true, alarmActive) else NotificationHelper.buildStandbyNotification(this))
            lastNotificationKey = key
        }
        session = sessions.read()
        val deadline = when {
            session.dismissedTarget == settings.targetPercentage || !battery.isPowerConnected -> 0L
            session.snoozedTarget == settings.targetPercentage -> session.snoozedUntil
            settings.repeatNotification && session.lastAlertedTarget == settings.targetPercentage -> session.lastAlertAt + ChargeAlertPolicy.REPEAT_INTERVAL_MS
            else -> 0L
        }
        if (deadline > 0 && battery.level >= settings.targetPercentage) {
            handler.postDelayed(evaluateLater, (deadline - now).coerceAtLeast(1000L))
        }
    }

    private fun publish(notification: Notification) {
        if (Build.VERSION.SDK_INT >= 34) startForeground(NotificationHelper.NOTIFICATION_ID_SERVICE,
            notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(NotificationHelper.NOTIFICATION_ID_SERVICE, notification)
        foregroundStarted = true
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NotificationHelper.NOTIFICATION_ID_RESUME)
    }

    private fun stopMonitoring() {
        isRunning = false
        NotificationHelper.cancelAlertNotification(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        scope.cancel()
        receiver?.let { unregisterReceiver(it) }
        NotificationHelper.cancelAlertNotification(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_REFRESH = "com.example.REFRESH_MONITOR"
        const val ACTION_RESUME = "com.example.RESUME_MONITOR"
        private val _runningState = kotlinx.coroutines.flow.MutableStateFlow(false)
        val runningState: kotlinx.coroutines.flow.StateFlow<Boolean> = _runningState
        @Volatile var isRunning = false
            private set(value) { field = value; _runningState.value = value }
        fun refresh(context: Context) {
            if (isRunning) context.startService(Intent(context, BatteryMonitoringService::class.java).setAction(ACTION_REFRESH))
        }
    }
}
