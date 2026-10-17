package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.PreferencesRepository
import com.example.receiver.StopAlarmReceiver
import com.example.service.BatteryMonitoringService
import com.example.ui.ChargingAlarmActivity

object NotificationHelper {
    const val CHANNEL_ALERT_ALARM_ID = "charge_alert_alarm_channel_v4"
    const val CHANNEL_ALERT_RINGTONE_ID = "charge_alert_ringtone_channel_v4"
    const val CHANNEL_ALERT_NOTIFICATION_ID = "charge_alert_notification_channel_v4"
    const val CHANNEL_SERVICE_ID = "charge_alert_monitoring_service_channel_v4"
    const val CHANNEL_LOCKSCREEN_ID = "charge_alert_lockscreen_channel_v4"
    const val CHANNEL_STANDBY_ID = "charge_alert_standby_channel_v6"
    const val NOTIFICATION_ID_ALERT = 1001
    const val NOTIFICATION_ID_SERVICE = 1002
    const val NOTIFICATION_ID_PLUGGED_IN = 1003
    const val NOTIFICATION_ID_STANDBY = 1004
    const val NOTIFICATION_ID_LOW_BATTERY = 1005
    const val NOTIFICATION_ID_TEMP_ALERT = 1006
    const val NOTIFICATION_ID_RESUME = 1007
    const val ACTION_DISMISS_SCREEN = "com.example.DISMISS_ALARM_ACTIVITY"

    private val handler = Handler(Looper.getMainLooper())
    private var timeout: Runnable? = null
    @Volatile private var player: MediaPlayer? = null
    private var ringtone: android.media.Ringtone? = null
    private var vibrator: Vibrator? = null
    private var audioFocus: android.media.AudioFocusRequest? = null
    private val _alarmState = kotlinx.coroutines.flow.MutableStateFlow(false)
    val alarmState: kotlinx.coroutines.flow.StateFlow<Boolean> = _alarmState
    @Volatile private var alarmActive = false
        set(value) { field = value; _alarmState.value = value }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            Triple(CHANNEL_ALERT_ALARM_ID, "Charge target alarm", NotificationManager.IMPORTANCE_HIGH),
            Triple(CHANNEL_ALERT_RINGTONE_ID, "Charge target ringtone", NotificationManager.IMPORTANCE_HIGH),
            Triple(CHANNEL_ALERT_NOTIFICATION_ID, "Charge target chime", NotificationManager.IMPORTANCE_HIGH),
            Triple(CHANNEL_SERVICE_ID, "Charging monitor", NotificationManager.IMPORTANCE_LOW),
            Triple(CHANNEL_STANDBY_ID, "Standby monitor", NotificationManager.IMPORTANCE_LOW),
            Triple(CHANNEL_LOCKSCREEN_ID, "Charging screen", NotificationManager.IMPORTANCE_HIGH)
        ).forEach { (id, name, importance) ->
            nm.createNotificationChannel(NotificationChannel(id, name, importance).apply {
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            })
        }
    }

    private fun canPost(context: Context, channel: String): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < 26 ||
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .getNotificationChannel(channel)?.importance != NotificationManager.IMPORTANCE_NONE
    }

    private fun post(context: Context, id: Int, notification: Notification) {
        try { (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(id, notification) }
        catch (_: SecurityException) { }
    }

    private fun screenIntent(context: Context, request: Int, alarm: Boolean = false, test: Boolean = false): PendingIntent =
        PendingIntent.getActivity(context, request, Intent(context, ChargingAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ChargingAlarmActivity.EXTRA_IS_ALARM, alarm)
            putExtra(ChargingAlarmActivity.EXTRA_IS_TEST, test)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun actionIntent(context: Context, action: String, test: Boolean = false): PendingIntent =
        PendingIntent.getBroadcast(context, if (test) 99 else action.hashCode(),
            Intent(context, StopAlarmReceiver::class.java).setAction(action)
                .putExtra(ChargingAlarmActivity.EXTRA_IS_TEST, test),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun canFullScreen(context: Context): Boolean = Build.VERSION.SDK_INT < 34 ||
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).canUseFullScreenIntent()

    fun showTargetReachedNotification(context: Context, currentLevel: Int, targetLevel: Int,
        soundEnabled: Boolean = true, vibrateEnabled: Boolean = true, soundType: String = "ALARM", isTest: Boolean = false): Boolean {
        if (!isTest && (currentLevel !in 0..100 || currentLevel < targetLevel)) return false
        createNotificationChannels(context)
        val channel = when (soundType.uppercase()) {
            "RINGTONE" -> CHANNEL_ALERT_RINGTONE_ID
            "NOTIFICATION" -> CHANNEL_ALERT_NOTIFICATION_ID
            else -> CHANNEL_ALERT_ALARM_ID
        }
        if (!canPost(context, channel)) return false
        val settings = PreferencesRepository(context).loadSettings()
        val en = settings.language == "en"
        val stop = actionIntent(context, StopAlarmReceiver.ACTION_STOP_ALARM, isTest)
        val screen = screenIntent(context, if (isTest) 91 else 1, true, isTest)
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_charge_alert)
            .setContentTitle(if (isTest) { if (en) "Test alarm" else "تجربة التنبيه" }
                else if (en) "Target $targetLevel% reached" else "وصل الشحن للهدف $targetLevel%")
            .setContentText(if (currentLevel < 0) { if (en) "Battery reading unavailable" else "قراءة البطارية غير متاحة" }
                else if (en) "Actual battery: $currentLevel%" else "نسبة البطارية الحالية: $currentLevel%")
            .setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false).setAutoCancel(false).setDeleteIntent(stop).setContentIntent(screen)
            .addAction(0, if (en) "Stop alarm" else "إيقاف التنبيه", stop)
            .apply { if (settings.isLockscreenEnabled && canFullScreen(context)) setFullScreenIntent(screen, true) }
            .build()
        stopAlarmSound(context)
        if (soundEnabled) playAlarmSoundBypassingSilent(context, soundType)
        alarmActive = true // A silent alert is active too.
        if (vibrateEnabled) startContinuousVibration(context)
        // Bound sound and vibration even if nobody interacts with the notification.
        scheduleStop(context, 60_000)
        post(context, NOTIFICATION_ID_ALERT, notification)
        return true
    }

    fun showPluggedInLockscreenNotification(context: Context) {
        createNotificationChannels(context)
        if (!canPost(context, CHANNEL_LOCKSCREEN_ID)) return
        val en = PreferencesRepository(context).loadSettings().language == "en"
        val screen = screenIntent(context, 2)
        post(context, NOTIFICATION_ID_PLUGGED_IN, NotificationCompat.Builder(context, CHANNEL_LOCKSCREEN_ID)
            .setSmallIcon(R.drawable.ic_stat_charge_alert)
            .setContentTitle(if (en) "Charger connected" else "تم توصيل الشاحن")
            .setContentText(if (en) "Tap to view charging. Shown once per connection." else "اضغط لعرض الشحن. يظهر مرة واحدة لكل توصيلة.")
            .setContentIntent(screen).setAutoCancel(true).setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply { if (canFullScreen(context)) setFullScreenIntent(screen, true) }.build())
    }

    fun buildServiceNotification(context: Context, currentLevel: Int, targetLevel: Int,
        isCharging: Boolean, isAlarmActive: Boolean = false): Notification {
        createNotificationChannels(context)
        val en = PreferencesRepository(context).loadSettings().language == "en"
        val title = when {
            isAlarmActive -> if (en) "Charge alert active • $currentLevel%" else "تنبيه الشحن نشط • $currentLevel%"
            currentLevel >= targetLevel -> if (en) "Target reached • $currentLevel%" else "تم الوصول للهدف • $currentLevel%"
            else -> if (en) "Battery $currentLevel% • Target $targetLevel%" else "البطارية $currentLevel% • الهدف $targetLevel%"
        }
        return NotificationCompat.Builder(context, CHANNEL_SERVICE_ID)
            .setSmallIcon(R.drawable.ic_stat_charge_alert).setContentTitle(title)
            .setContentText(if (en) "Monitoring this charging session" else "مراقبة جلسة الشحن الحالية")
            .setProgress(100, currentLevel.coerceIn(0, 100), currentLevel < 0)
            .setPriority(NotificationCompat.PRIORITY_LOW).setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true).setOnlyAlertOnce(true).setShowWhen(false)
            .setContentIntent(screenIntent(context, 3, isAlarmActive))
            .setDeleteIntent(actionIntent(context, StopAlarmReceiver.ACTION_DISMISS_MONITOR_NOTIFICATION))
            .apply {
                if (isAlarmActive) addAction(0, if (en) "Stop alarm" else "إيقاف التنبيه",
                    actionIntent(context, StopAlarmReceiver.ACTION_STOP_ALARM))
                addAction(0, if (en) "Pause this session" else "إيقاف هذه الجلسة",
                    actionIntent(context, StopAlarmReceiver.ACTION_PAUSE_MONITOR))
            }.build()
    }

    fun buildStandbyNotification(context: Context): Notification {
        createNotificationChannels(context)
        val en = PreferencesRepository(context).loadSettings().language == "en"
        val main = PendingIntent.getActivity(context, 4, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, CHANNEL_STANDBY_ID)
            .setSmallIcon(R.drawable.ic_stat_charge_alert)
            .setContentTitle(if (en) "Charge monitor ready" else "مراقبة الشحن جاهزة")
            .setContentText(if (en) "Waiting for a charger" else "بانتظار توصيل الشاحن")
            .setPriority(NotificationCompat.PRIORITY_LOW).setOngoing(true).setOnlyAlertOnce(true).setShowWhen(false)
            .setContentIntent(main)
            .setDeleteIntent(actionIntent(context, StopAlarmReceiver.ACTION_DISMISS_MONITOR_NOTIFICATION))
            .addAction(0, if (en) "Pause monitoring" else "إيقاف المراقبة",
                actionIntent(context, StopAlarmReceiver.ACTION_PAUSE_MONITOR)).build()
    }

    fun showResumeMonitoringNotification(context: Context) {
        createNotificationChannels(context)
        if (!canPost(context, CHANNEL_SERVICE_ID)) return
        val en = PreferencesRepository(context).loadSettings().language == "en"
        val intent = Intent(context, BatteryMonitoringService::class.java).setAction(BatteryMonitoringService.ACTION_RESUME)
        val pending = if (Build.VERSION.SDK_INT >= 26) PendingIntent.getForegroundService(context, 7, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        else PendingIntent.getService(context, 7, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        post(context, NOTIFICATION_ID_RESUME, NotificationCompat.Builder(context, CHANNEL_SERVICE_ID)
            .setSmallIcon(R.drawable.ic_stat_charge_alert)
            .setContentTitle(if (en) "Tap to start charge monitoring" else "اضغط لبدء مراقبة الشحن")
            .setContentText(if (en) "Android requires you to resume monitoring" else "أندرويد يتطلب تدخلك لاستئناف المراقبة")
            .setContentIntent(pending).setAutoCancel(true).setOnlyAlertOnce(true)
            .setDeleteIntent(actionIntent(context, StopAlarmReceiver.ACTION_PAUSE_MONITOR)).build())
    }

    fun showLowBatteryNotification(context: Context, currentLevel: Int, threshold: Int, soundEnabled: Boolean, vibrateEnabled: Boolean) {
        val en = PreferencesRepository(context).loadSettings().language == "en"
        showSafety(context, NOTIFICATION_ID_LOW_BATTERY,
            if (en) "Low battery • $currentLevel%" else "البطارية منخفضة • $currentLevel%",
            if (en) "Reminder threshold: $threshold%" else "حد التذكير: $threshold%", soundEnabled, vibrateEnabled)
    }

    fun showTemperatureAlertNotification(context: Context, currentTemp: Float, threshold: Int, soundEnabled: Boolean, vibrateEnabled: Boolean) {
        val en = PreferencesRepository(context).loadSettings().language == "en"
        showSafety(context, NOTIFICATION_ID_TEMP_ALERT,
            if (en) "Battery temperature • $currentTemp°C" else "حرارة البطارية • $currentTemp°م",
            if (en) "Above your $threshold°C limit. Check the device." else "تجاوزت الحد الذي حددته $threshold°م. افحص الهاتف.",
            soundEnabled, vibrateEnabled)
    }

    private fun showSafety(context: Context, id: Int, title: String, text: String, sound: Boolean, vibrate: Boolean) {
        val channel = "battery_safety_${id}_${sound}_${vibrate}"
        if (Build.VERSION.SDK_INT >= 26) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(NotificationChannel(channel, "Battery reminder", NotificationManager.IMPORTANCE_HIGH).apply {
                    setSound(if (sound) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) else null,
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
                    enableVibration(vibrate)
                })
        }
        if (!canPost(context, channel)) return
        val main = PendingIntent.getActivity(context, id, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        post(context, id, NotificationCompat.Builder(context, channel).setSmallIcon(R.drawable.ic_stat_charge_alert)
            .setContentTitle(title).setContentText(text).setContentIntent(main).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults((if (sound) NotificationCompat.DEFAULT_SOUND else 0) or (if (vibrate) NotificationCompat.DEFAULT_VIBRATE else 0))
            .build())
    }

    fun playAlarmSoundBypassingSilent(context: Context, soundType: String) {
        stopAlarmSound(context)
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        if (Build.VERSION.SDK_INT >= 26) {
            audioFocus = android.media.AudioFocusRequest.Builder(android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(attrs).build().also { am.requestAudioFocus(it) }
        }
        try {
            val mp = MediaPlayer()
            player = mp // Assign before prepareAsync so callbacks can identify the current player.
            mp.setAudioAttributes(attrs)
            mp.setDataSource(context.applicationContext, getSoundUri(soundType))
            mp.isLooping = true
            mp.setOnPreparedListener { if (player === it) it.start() }
            mp.setOnErrorListener { failed, _, _ ->
                if (player === failed) {
                    failed.release()
                    player = null
                    ringtone = RingtoneManager.getRingtone(context, getSoundUri(soundType))?.apply {
                        audioAttributes = attrs
                        if (Build.VERSION.SDK_INT >= 28) isLooping = true
                        play()
                    }
                }
                true
            }
            mp.prepareAsync()
        } catch (_: Exception) {
            player?.release()
            player = null
        }
        scheduleStop(context, 60_000)
    }

    private fun scheduleStop(context: Context, delay: Long) {
        timeout?.let(handler::removeCallbacks)
        timeout = Runnable {
            stopAlarmSound(context)
            BatteryMonitoringService.refresh(context)
        }.also { handler.postDelayed(it, delay) }
    }

    private fun startContinuousVibration(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= 31) (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            else @Suppress("DEPRECATION") (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        val pattern = longArrayOf(0, 500, 500)
        if (Build.VERSION.SDK_INT >= 26) vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        else @Suppress("DEPRECATION") vibrator?.vibrate(pattern, 0)
    }

    fun stopContinuousVibration() { vibrator?.cancel(); vibrator = null }

    fun stopAlarmSound(context: Context? = null) {
        timeout?.let(handler::removeCallbacks)
        timeout = null
        alarmActive = false
        stopContinuousVibration()
        ringtone?.stop()
        ringtone = null
        player?.release()
        player = null
        if (Build.VERSION.SDK_INT >= 26 && context != null) {
            audioFocus?.let { (context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager).abandonAudioFocusRequest(it) }
        }
        audioFocus = null
    }

    fun cancelAlertNotification(context: Context) {
        stopAlarmSound(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID_ALERT)
        nm.cancel(NOTIFICATION_ID_PLUGGED_IN)
    }

    fun isAlarmActive() = alarmActive
    fun isAlarmSounding() = alarmActive || player?.isPlaying == true || ringtone?.isPlaying == true

    fun getSoundUri(soundType: String): android.net.Uri =
        RingtoneManager.getDefaultUri(when (soundType.uppercase()) {
            "RINGTONE" -> RingtoneManager.TYPE_RINGTONE
            "NOTIFICATION" -> RingtoneManager.TYPE_NOTIFICATION
            else -> RingtoneManager.TYPE_ALARM
        }) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
}
