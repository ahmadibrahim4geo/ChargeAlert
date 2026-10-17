package com.example.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChargingSessionStore
import com.example.data.PreferencesRepository
import com.example.model.AlertSettings
import com.example.model.BatteryInfo
import com.example.service.BatteryMonitoringService
import com.example.ui.theme.LOCKSCREEN_THEMES_LIST
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RenderLockscreenTheme
import com.example.ui.theme.hasChargingAnimation
import com.example.util.NotificationHelper

class ChargingAlarmActivity : ComponentActivity() {
    companion object {
        const val EXTRA_IS_ALARM = "com.example.EXTRA_IS_ALARM"
        const val EXTRA_IS_TEST = "com.example.EXTRA_IS_TEST"
    }

    private val viewModel: MainViewModel by viewModels()
    private var contentVisible by mutableStateOf(false)
    private var testMode by mutableStateOf(false)
    private var dismissReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        testMode = intent.getBooleanExtra(EXTRA_IS_TEST, false)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        // The normal system screen timeout applies; no wake lock or permanent KEEP_SCREEN_ON.
        dismissReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) { finish() }
        }
        ContextCompat.registerReceiver(this, dismissReceiver, IntentFilter(NotificationHelper.ACTION_DISMISS_SCREEN),
            ContextCompat.RECEIVER_NOT_EXPORTED)
        setContent {
            MyApplicationTheme(darkTheme = true) {
                BackHandler { closeScreen() }
                if (contentVisible) {
                    val battery by viewModel.batteryInfo.collectAsStateWithLifecycle()
                    val settings by viewModel.alertSettings.collectAsStateWithLifecycle()
                    val active by NotificationHelper.alarmState.collectAsStateWithLifecycle()
                    val powerSave = (getSystemService(POWER_SERVICE) as PowerManager).isPowerSaveMode
                    ChargingScreen(battery, settings, testMode, active, powerSave,
                        isAppearancePreview = testMode && !intent.getBooleanExtra(EXTRA_IS_ALARM, false),
                        onClose = ::closeScreen,
                        onSnooze = ::snooze,
                        onThemeSelected = viewModel::setLockscreenThemeId,
                        onAnimationsChanged = { PreferencesRepository(this).updateAnimationsEnabled(it) })
                }
            }
        }
    }

    private fun closeScreen() {
        if (testMode && !intent.getBooleanExtra(EXTRA_IS_ALARM, false)) {
            finish()
            return
        }
        val store = ChargingSessionStore(this)
        val battery = viewModel.batteryInfo.value
        val settings = viewModel.alertSettings.value
        if (!testMode) store.write(store.read().copy(screenPresented = true))
        if (!testMode && battery.isPowerConnected && battery.level >= settings.targetPercentage) {
            store.dismissTarget(settings.targetPercentage)
        }
        NotificationHelper.cancelAlertNotification(this)
        BatteryMonitoringService.refresh(this)
        finish()
    }

    private fun snooze() {
        if (!testMode) {
            ChargingSessionStore(this).snooze(viewModel.alertSettings.value.targetPercentage,
                System.currentTimeMillis() + 5 * 60 * 1000)
        }
        NotificationHelper.cancelAlertNotification(this)
        BatteryMonitoringService.refresh(this)
        finish()
    }

    override fun onStart() { super.onStart(); contentVisible = true }
    override fun onStop() { contentVisible = false; super.onStop() }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        testMode = intent.getBooleanExtra(EXTRA_IS_TEST, false)
    }
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
            closeScreen()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    override fun onDestroy() {
        dismissReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }
}

@Composable
fun ChargingScreen(
    battery: BatteryInfo, settings: AlertSettings, isTest: Boolean = false, alarmActive: Boolean = false,
    powerSave: Boolean = false, isAppearancePreview: Boolean = false, onClose: () -> Unit, onSnooze: () -> Unit = {},
    onThemeSelected: (String) -> Unit = {}, onAnimationsChanged: (Boolean) -> Unit = {}
) {
    val en = settings.language == "en"
    val theme = LOCKSCREEN_THEMES_LIST.find { it.id == settings.lockscreenThemeId } ?: LOCKSCREEN_THEMES_LIST.first()
    val accent = theme.accentColor
    val reached = battery.level in settings.targetPercentage..100 && battery.isPowerConnected
    val animationEnabled = settings.animationsEnabled && !powerSave && battery.isCharging && battery.level in 0..100
    val motionHint = when {
        powerSave -> if (en) "Motion paused by Battery Saver" else "الحركة متوقفة لتوفير الطاقة"
        !settings.animationsEnabled -> if (en) "Motion is turned off in your settings" else "الحركة مقفولة من إعداداتك"
        !hasChargingAnimation(theme.id) -> if (en) "This design has a still display" else "هذا التصميم هادئ وثابت"
        !battery.isCharging -> if (en) "The charging pulse starts when charging resumes" else "النبضة تبدأ عندما يكون الشحن شغّالًا"
        theme.id == "NEON_RING" -> if (en) "The pulse means charging is active; the arc follows the real percentage"
            else "النبضة تعني أن الشحن شغّال؛ طول القوس يتبع النسبة الحقيقية"
        else -> if (en) "Gentle motion while charging" else "حركة هادئة أثناء الشحن"
    }
    val status = when {
        isAppearancePreview -> if (en) "DESIGN PREVIEW" else "معاينة المظهر"
        isTest -> if (en) "ALARM PREVIEW" else "معاينة التنبيه"
        alarmActive -> if (en) "CHARGE ALERT" else "تنبيه الشحن"
        reached -> if (en) "TARGET REACHED" else "وصلنا للهدف"
        battery.isCharging -> if (en) "CHARGING" else "جارِ الشحن"
        battery.isPowerConnected -> if (en) "CHARGING PAUSED" else "الشحن متوقف مؤقتاً"
        else -> if (en) "BATTERY STATUS" else "حالة البطارية"
    }
    CompositionLocalProvider(LocalLayoutDirection provides if (en) LayoutDirection.Ltr else LayoutDirection.Rtl) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(theme.backgroundColor, Color(0xFF03070D))))) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().systemBarsPadding().testTag("charging_content"),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("CHARGE ALERT", color = accent, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(if (en) theme.nameEn else theme.nameAr, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onClose, modifier = Modifier.size(48.dp).background(Color(0x18FFFFFF), CircleShape)) {
                            Icon(Icons.Default.Close, if (en) "Close charging screen" else "إغلاق شاشة الشحن", tint = Color.White)
                        }
                    }
                }
                item {
                    Surface(color = accent.copy(alpha = 0.10f), shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))) {
                        Text(status, color = accent, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        RenderLockscreenTheme(settings.lockscreenThemeId, battery, settings.targetPercentage, reached,
                            modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().aspectRatio(1f),
                            animated = animationEnabled)
                    }
                    if (theme.id == "NEON_RING") {
                        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).background(accent, CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text(if (en) "Current charge" else "شحنك الحالي", color = accent, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(3.dp).height(13.dp).background(Color.White))
                                Spacer(Modifier.width(6.dp))
                                Text(if (en) "Target ${settings.targetPercentage}%" else "هدفك ${settings.targetPercentage}%",
                                    color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    Text(motionHint, modifier = Modifier.fillMaxWidth().testTag("charging_motion_hint"),
                        color = Color(0xFFC0CED8), fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text(if (en) "Live battery reading from Android" else "قراءة البطارية مباشرة من أندرويد",
                        color = Color(0xFF8E9DAE), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Surface(color = Color(0xFF101C29), shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFF213247))) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (en) "Your charge target" else "هدف الشحن", color = Color(0xFFB4C0CE))
                                Text("${settings.targetPercentage}%", color = accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(when {
                                isTest -> if (en) "Preview only. Your real charge target is unchanged." else "معاينة فقط. هدف الشحن الحقيقي محفوظ."
                                reached -> if (en) "You can unplug your charger now." else "يمكنك فصل الشاحن الآن."
                                battery.level < 0 -> if (en) "Waiting for a battery reading…" else "بانتظار قراءة البطارية…"
                                else -> if (en) "${(settings.targetPercentage - battery.level).coerceAtLeast(0)}% to your target"
                                    else "باقي ${(settings.targetPercentage - battery.level).coerceAtLeast(0)}% للوصول لهدفك"
                            }, color = Color(0xFFB4C0CE), fontSize = 13.sp)
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChargingMetric(if (en) "Temperature" else "الحرارة",
                            if (battery.temperature.isFinite()) "${battery.temperature}°C" else "—", Modifier.weight(1f))
                        ChargingMetric(if (en) "Battery voltage" else "جهد البطارية",
                            if (battery.voltage > 0) "${battery.voltage} mV" else "—", Modifier.weight(1f))
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color(0xFF031017))) {
                            Text(if (alarmActive) { if (en) "Stop alarm" else "إيقاف التنبيه" }
                                else if (en) "Close charging screen" else "إغلاق شاشة الشحن", fontWeight = FontWeight.Bold)
                        }
                        if (alarmActive) OutlinedButton(onClick = onSnooze, modifier = Modifier.fillMaxWidth()) {
                            Text(if (en) "Remind me in 5 minutes" else "ذكّرني بعد ٥ دقائق", color = Color.White)
                        }
                        Text(if (en) "Closing this screen keeps monitoring active. A stopped alert stays dismissed for this session."
                            else "إغلاق الشاشة يُبقي المراقبة شغالة. إيقاف التنبيه يمنع تكراره في نفس جلسة الشحن.",
                            color = Color(0xFF8E9DAE), fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (en) "Animated designs" else "تصميمات متحركة", color = Color.White)
                        Switch(checked = settings.animationsEnabled, onCheckedChange = onAnimationsChanged)
                    }
                    if (powerSave) Text(if (en) "Quiet display while Battery Saver is on" else "عرض هادئ أثناء تشغيل توفير الطاقة",
                        color = Color(0xFF8E9DAE), fontSize = 12.sp)
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(LOCKSCREEN_THEMES_LIST, key = { it.id }) { design ->
                            FilterChip(selected = design.id == settings.lockscreenThemeId,
                                onClick = { onThemeSelected(design.id) },
                                label = { Text(if (en) design.nameEn else design.nameAr) },
                                leadingIcon = { Box(Modifier.size(8.dp).background(design.accentColor, CircleShape)) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChargingMetric(label: String, value: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(20.dp), color = Color(0xFF0E1925)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, color = Color(0xFF8E9DAE), fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
