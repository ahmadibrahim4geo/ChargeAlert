package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AlertSettings
import com.example.model.BatteryInfo
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val alertSettings by viewModel.alertSettings.collectAsState()
    val permissionState by viewModel.permissionState.collectAsState()
    val monitoringActive by com.example.service.BatteryMonitoringService.runningState.collectAsState()

    val isEn = alertSettings.language == "en"
    val layoutDirection = if (isEn) LayoutDirection.Ltr else LayoutDirection.Rtl

    val hasNotificationPermission = permissionState.hasNotificationPermission
    val isIgnoringBatteryOptimizations = permissionState.isIgnoringBatteryOptimizations
    val canUseFullScreenIntent = permissionState.canUseFullScreenIntent
    val isOemAutoStartAcknowledged = permissionState.isOemAutoStartAcknowledged

    val oemAutoStartIntent = remember {
        com.example.util.PowerManagementUtil.getOemAutoStartIntent(context)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.refreshPermissions()
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isEn) "Charge Alert" else "تنبيه الشحن • Charge Alert",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                            Text(
                                text = if (isEn) {
                                    if (batteryInfo.isCharging) "Charging Monitor Active" else "Plug in charger to monitor"
                                } else {
                                    if (batteryInfo.isCharging) "مراقب الشحن نشط حالياً" else "قم بتوصيل الشاحن لبدء المراقبة"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        Surface(
                            shape = CircleShape,
                            color = if (alertSettings.isAlertEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (alertSettings.isAlertEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (alertSettings.isAlertEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (alertSettings.isAlertEnabled) (if (isEn) "Alert ON" else "التنبيه مفعل") else (if (isEn) "Alert OFF" else "التنبيه معطل"),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (alertSettings.isAlertEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        Triple(0, if (isEn) "Dashboard" else "الرئيسية", Icons.Default.BatteryChargingFull),
                        Triple(1, if (isEn) "Alerts" else "التنبيهات", Icons.Default.NotificationsActive),
                        Triple(2, if (isEn) "Themes" else "المظاهر", Icons.Default.Palette),
                        Triple(3, if (isEn) "Guardian" else "الحارس والصحة", Icons.Default.Security)
                    )
                    tabs.forEach { (index, title, icon) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(icon, contentDescription = title) },
                            label = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // ── TAB 0: DASHBOARD ─────────────────────────────────
                        item {
                            LanguageSelectorCard(
                                currentLang = alertSettings.language,
                                onLanguageSelected = { viewModel.setLanguage(it) }
                            )
                        }

                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            item {
                                PermissionCard(
                                    isEn = isEn,
                                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                    onOpenAppSettings = { viewModel.openAppSettings(context) }
                                )
                            }
                        }

                        item {
                            MasterToggleCard(
                                isEn = isEn,
                                isAlertEnabled = alertSettings.isAlertEnabled,
                                onAlertEnabledChanged = { viewModel.setAlertEnabled(it) }
                            )
                        }

                        item {
                            BatteryGaugeCard(
                                isEn = isEn,
                                batteryInfo = batteryInfo,
                                targetPercentage = alertSettings.targetPercentage,
                                isAlertEnabled = alertSettings.isAlertEnabled
                            )
                        }

                        item {
                            ChargingTelemetryCard(
                                isEn = isEn,
                                batteryInfo = batteryInfo,
                                targetPercentage = alertSettings.targetPercentage
                            )
                        }

                        item {
                            AppStatusCard(
                                isEn = isEn,
                                isAlertEnabled = alertSettings.isAlertEnabled,
                                isCharging = batteryInfo.isPowerConnected,
                                monitoringActive = monitoringActive,
                                onResumeMonitoring = { viewModel.setAlertEnabled(true) },
                                targetPercentage = alertSettings.targetPercentage,
                                hasNotificationPermission = hasNotificationPermission,
                                isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
                                canUseFullScreenIntent = canUseFullScreenIntent
                            )
                        }
                    }

                    1 -> {
                        // ── TAB 1: ALERTS & SAFETY ───────────────────────────
                        item {
                            Box(modifier = Modifier.alpha(if (alertSettings.isAlertEnabled) 1f else 0.4f)) {
                                TargetSettingCard(
                                    isEn = isEn,
                                    targetPercentage = alertSettings.targetPercentage,
                                    currentLevel = batteryInfo.level,
                                    onTargetChanged = { if (alertSettings.isAlertEnabled) viewModel.setTargetPercentage(it) }
                                )
                            }
                        }

                        item {
                            Box(modifier = Modifier.alpha(if (alertSettings.isAlertEnabled) 1f else 0.4f)) {
                                LowBatterySettingCard(
                                    isEn = isEn,
                                    isEnabled = alertSettings.isLowBatteryAlertEnabled,
                                    lowPercentage = alertSettings.lowBatteryPercentage,
                                    currentLevel = batteryInfo.level,
                                    onEnabledChanged = { if (alertSettings.isAlertEnabled) viewModel.setLowBatteryAlertEnabled(it) },
                                    onPercentageChanged = { if (alertSettings.isAlertEnabled) viewModel.setLowBatteryPercentage(it) }
                                )
                            }
                        }

                        item {
                            Box(modifier = Modifier.alpha(if (alertSettings.isAlertEnabled) 1f else 0.4f)) {
                                TemperatureAlertCard(
                                    isEn = isEn,
                                    isEnabled = alertSettings.isTemperatureAlertEnabled,
                                    threshold = alertSettings.temperatureThreshold,
                                    currentTemp = batteryInfo.temperature,
                                    onEnabledChanged = { if (alertSettings.isAlertEnabled) viewModel.setTemperatureAlertEnabled(it) },
                                    onThresholdChanged = { if (alertSettings.isAlertEnabled) viewModel.setTemperatureThreshold(it) }
                                )
                            }
                        }

                        item {
                            Box(modifier = Modifier.alpha(if (alertSettings.isAlertEnabled) 1f else 0.4f)) {
                                AlertControlsCard(
                                    isEn = isEn,
                                    alertSettings = alertSettings,
                                    onAlertEnabledChanged = { viewModel.setAlertEnabled(it) },
                                    onSoundEnabledChanged = { if (alertSettings.isAlertEnabled) viewModel.setSoundEnabled(it) },
                                    onVibrateEnabledChanged = { if (alertSettings.isAlertEnabled) viewModel.setVibrateEnabled(it) },
                                    onRepeatNotificationChanged = { if (alertSettings.isAlertEnabled) viewModel.setRepeatNotification(it) },
                                    onTestNotification = { if (alertSettings.isAlertEnabled) viewModel.testNotification() },
                                    onSoundTypeSelected = { if (alertSettings.isAlertEnabled) viewModel.setSoundType(it) },
                                    onPreviewSound = { if (alertSettings.isAlertEnabled) viewModel.previewSound(it) },
                                    onStopPreview = { viewModel.stopSoundPreview() }
                                )
                            }
                        }
                    }

                    2 -> {
                        // ── TAB 2: THEMES ────────────────────────────────────
                        item {
                            Box(modifier = Modifier.alpha(if (alertSettings.isAlertEnabled) 1f else 0.4f)) {
                                LockscreenThemeSelectorCard(
                                    isEn = isEn,
                                    alertSettings = alertSettings,
                                    onLockscreenEnabledChanged = { if (alertSettings.isAlertEnabled) viewModel.setLockscreenEnabled(it) },
                                    onThemeSelected = { if (alertSettings.isAlertEnabled) viewModel.setLockscreenThemeId(it) },
                                    onPreviewTheme = {
                                        if (alertSettings.isAlertEnabled) {
                                            val intent = android.content.Intent(context, ChargingAlarmActivity::class.java)
                                                .putExtra(ChargingAlarmActivity.EXTRA_IS_TEST, true)
                                            context.startActivity(intent)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    3 -> {
                        // ── TAB 3: GUARDIAN & HEALTH ─────────────────────────
                        item {
                            Box(modifier = Modifier.alpha(if (alertSettings.isAlertEnabled) 1f else 0.4f)) {
                                AlwaysOnMonitorCard(
                                    isEn = isEn,
                                    isAlwaysOnEnabled = alertSettings.isAlwaysOnMonitorEnabled,
                                    onAlwaysOnChanged = {
                                        if (alertSettings.isAlertEnabled) {
                                            viewModel.updateAlwaysOnMonitorEnabled(it)
                                        }
                                    },
                                    onOpenNotificationSettings = {
                                        viewModel.openStandbyChannelSettings(context)
                                    }
                                )
                            }
                        }

                        if (!isIgnoringBatteryOptimizations && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            item {
                                BatteryOptimizationCard(
                                    isEn = isEn,
                                    onRequestOptimizationExemption = { viewModel.requestIgnoreBatteryOptimization(context) },
                                    onRefreshStatus = { viewModel.refreshPermissions() }
                                )
                            }
                        }

                        if (!canUseFullScreenIntent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            item {
                                FullScreenIntentPermissionCard(
                                    isEn = isEn,
                                    onRequestPermission = { com.example.util.PowerManagementUtil.requestFullScreenIntentPermission(context) }
                                )
                            }
                        }

                        if (oemAutoStartIntent != null && !isOemAutoStartAcknowledged) {
                            item {
                                OemAutoStartCard(
                                    isEn = isEn,
                                    onOpenSettings = {
                                        try { context.startActivity(oemAutoStartIntent) } catch (_: Exception) {}
                                    },
                                    onDismissCard = { viewModel.acknowledgeOemAutoStart() }
                                )
                            }
                        }

                        item {
                            BatteryHealthTipsCard(isEn = isEn)
                        }

                        item {
                            BatteryMetricsCard(
                                isEn = isEn,
                                batteryInfo = batteryInfo
                            )
                        }

                        item {
                            DeveloperFooterCard(isEn = isEn)
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun LanguageSelectorCard(
    currentLang: String,
    onLanguageSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (currentLang == "en") "Language • اللغة" else "اللغة • Language",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentLang == "ar",
                    onClick = { onLanguageSelected("ar") },
                    label = { Text("العربية", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = currentLang == "en",
                    onClick = { onLanguageSelected("en") },
                    label = { Text("English", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun AlwaysOnMonitorCard(
    isEn: Boolean,
    isAlwaysOnEnabled: Boolean,
    onAlwaysOnChanged: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlwaysOnEnabled)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isAlwaysOnEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isAlwaysOnEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isAlwaysOnEnabled) Icons.Default.Bolt else Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = if (isAlwaysOnEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isEn) "Instant Background Wake-Up" else "الاستجابة التلقائية عند الشحن",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) "Automatic plug-in detection" else "حارس الشاحن الذكي في الخلفية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isAlwaysOnEnabled,
                    onCheckedChange = onAlwaysOnChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isAlwaysOnEnabled) {
                    if (isEn)
                        "Keeps a silent foreground monitor ready for charger connections. Android may still restrict background operation."
                    else
                        "تبقى المراقبة جاهزة في الخلفية بإشعار صامت لاكتشاف توصيل الشاحن. قد يفرض أندرويد قيوداً على التشغيل في الخلفية."
                } else {
                    if (isEn)
                        "Stops the foreground monitor while unplugged. Open the app after connecting the charger if Android delays automatic monitoring."
                    else
                        "تتوقف خدمة المراقبة عند فصل الشاحن. افتح التطبيق بعد توصيل الشاحن إذا أخّر أندرويد بدء المراقبة تلقائياً."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            if (isAlwaysOnEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onOpenNotificationSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Hide Standby Notification in Settings" else "🔕 إخفاء إشعار الخلفية من إعدادات النظام",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEn)
                        "Android manages foreground service visibility. Use the notification action to pause monitoring."
                    else
                        "أندرويد يدير ظهور إشعار خدمة الخلفية. استخدم زر الإشعار لإيقاف المراقبة.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ChargingTelemetryCard(
    isEn: Boolean,
    batteryInfo: BatteryInfo,
    targetPercentage: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Battery measurements" else "قياسات البطارية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        batteryInfo.wattage >= 30f -> Color(0xFF00E676).copy(alpha = 0.2f)
                        batteryInfo.wattage >= 15f -> MaterialTheme.colorScheme.primaryContainer
                        batteryInfo.isCharging -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                ) {
                    Text(
                        text = if (isEn) batteryInfo.chargingSpeedCategory else when (batteryInfo.chargingSpeedCategory) {
                            "Full" -> "مكتمل"
                            "Charging" -> "جارِ الشحن"
                            "Charging Paused" -> "الشحن متوقف مؤقتاً"
                            "Slow Charging"    -> "🐢 شحن بطيء"
                            else               -> "🔋 في وضع التفريغ"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (batteryInfo.wattage >= 30f) Color(0xFF00E676) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (batteryInfo.isCharging && batteryInfo.wattage > 0) "${batteryInfo.wattage} W" else "-- W",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isEn) "Net battery power" else "صافي قدرة البطارية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val currentText = if (batteryInfo.hasCurrentReading) {
                        val sign = if (batteryInfo.currentNowMa > 0) "+" else ""
                        "$sign${batteryInfo.currentNowMa} mA"
                    } else {
                        "-- mA"
                    }
                    Text(
                        text = currentText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (batteryInfo.currentNowMa >= 0) Color(0xFF00E5FF) else Color(0xFFFFAB00)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isEn) "Flow Current" else "تدفق التيار اللحظي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (batteryInfo.isCharging && batteryInfo.estimatedMinutesRemaining > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn)
                            "Est. ~${batteryInfo.estimatedMinutesRemaining} min left to reach $targetPercentage%"
                        else
                            "الوقت التقديري المتبقي: حوالي ${batteryInfo.estimatedMinutesRemaining} دقيقة للوصول إلى $targetPercentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun LowBatterySettingCard(
    isEn: Boolean,
    isEnabled: Boolean,
    lowPercentage: Int,
    currentLevel: Int,
    onEnabledChanged: (Boolean) -> Unit,
    onPercentageChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = Color(0xFFFF9100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEn) "Low Battery Reminder" else "تنبيه انخفاض البطارية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) "Alert before battery depletes" else "تنبيه لتوصيل الشاحن في الوقت المناسب",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onEnabledChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = Color(0xFFFF9100)
                    )
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Alert when battery drops to:" else "تنبيه عندما تنخفض البطارية إلى:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$lowPercentage%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF9100)
                    )
                }

                Slider(
                    value = lowPercentage.toFloat(),
                    onValueChange = { onPercentageChanged(it.toInt()) },
                    valueRange = 10f..35f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF9100),
                        activeTrackColor = Color(0xFFFF9100)
                    )
                )

                Text(
                    text = if (isEn)
                        "💡 Tip: Lithium batteries degrade faster when drained below 20%. Keeping it above 20% preserves battery health."
                    else
                        "💡 نصيحة: ينصح بتوصيل الشاحن قبل نزول البطارية عن 20% لتجنب إجهاد خلايا الليثيوم والتفريغ العميق.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TemperatureAlertCard(
    isEn: Boolean,
    isEnabled: Boolean,
    threshold: Int,
    currentTemp: Float,
    onEnabledChanged: (Boolean) -> Unit,
    onThresholdChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEn) "Overheat Protection Alarm" else "حماية البطارية من الحرارة المرتفعة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) "Current: ${currentTemp}°C" else "الحرارة الحالية: ${currentTemp}°م",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentTemp >= threshold) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onEnabledChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = Color(0xFFFF5252)
                    )
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Alert threshold:" else "حد الإنذار الطارئ:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$threshold°C",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF5252)
                    )
                }

                Slider(
                    value = threshold.toFloat(),
                    onValueChange = { onThresholdChanged(it.toInt()) },
                    valueRange = 38f..48f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF5252),
                        activeTrackColor = Color(0xFFFF5252)
                    )
                )

                Text(
                    text = if (isEn)
                        "🔥 Caution: Fast chargers heat up the battery. Above 42°C, lithium degradation accelerates exponentially!"
                    else
                        "🔥 تحذير: الشواحن السريعة ترفع حرارة الهاتف. حرارة البطارية فوق 42°م هي العدو الأول للبطارية وتسبب انتفاخها وتلفها.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BatteryHealthTipsCard(isEn: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Battery Longevity Guidelines" else "إرشادات علمية لحماية بطاريتك",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tips = if (isEn) listOf(
                "⚡ Golden 20% - 80% Rule: Charging to 80% instead of 100% triples battery cycle lifespan (up to 1,500 charge cycles).",
                "🌡️ Avoid Heavy Gaming While Charging: High CPU usage combined with fast charging causes damaging heat spikes.",
                "🔌 Use Certified Cables & Adapters: Inconsistent voltage ripples from cheap chargers harm the battery controller chip.",
                "📱 Remove Bulky Cases During Turbo Charging: Thick cases trap heat and prevent natural passive cooling."
            ) else listOf(
                "⚡ قاعدة الذهب (20% إلى 80%): إيقاف الشحن عند 80% يضاعف عمر البطارية 3 مرات ويحميها من إجهاد الجهد العالي (حتى 1500 دورة شحن).",
                "🌡️ تجنب الألعاب الثقيلة أثناء الشحن: تشغيل ألعاب ثقيلة أثناء الشحن السريع يرفع الحرارة لمستويات خطرة تسرّع شيخوخة الخلايا.",
                "🔌 استخدم كابلات وشواحن معتمدة: الشواحن الرديئة تنتج جهداً غير مستقر يضر بدارة الشحن وبطارية الهاتف.",
                "📱 انزع الجراب السميك عند الشحن السريع: الأغطية السميكة تحبس الحرارة وتمنع التبريد الطبيعي للهاتف."
            )

            tips.forEach { tip ->
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun LockscreenThemeSelectorCard(
    isEn: Boolean,
    alertSettings: AlertSettings,
    onLockscreenEnabledChanged: (Boolean) -> Unit,
    onThemeSelected: (String) -> Unit,
    onPreviewTheme: () -> Unit
) {
    ChargingAppearanceCard(isEn, alertSettings, onLockscreenEnabledChanged, onThemeSelected, onPreviewTheme)
}
@Composable
fun BatteryGaugeCard(
    isEn: Boolean,
    batteryInfo: BatteryInfo,
    targetPercentage: Int,
    isAlertEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("battery_gauge_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val animatedLevel by animateFloatAsState(
                targetValue = batteryInfo.level.coerceIn(0, 100).toFloat(),
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "battery_level_animation"
            )

            val isCharging = batteryInfo.isCharging

            val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_alpha"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(230.dp)
                    .padding(8.dp)
            ) {
                val activePrimary = MaterialTheme.colorScheme.primary
                val activeSecondary = MaterialTheme.colorScheme.secondary
                val trackColor = MaterialTheme.colorScheme.surface

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 22.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val radius = diameter / 2f
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)

                    // Background Track Ring
                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Current Level Arc
                    val currentSweep = (animatedLevel / 100f) * 270f
                    val arcBrush = Brush.linearGradient(
                        colors = listOf(activePrimary, activeSecondary)
                    )

                    if (isCharging) {
                        // Ambient charge glow
                        drawArc(
                            color = activePrimary.copy(alpha = 0.15f * pulseAlpha),
                            startAngle = 135f,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth + 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    drawArc(
                        brush = arcBrush,
                        startAngle = 135f,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Target Marker Dot
                    val targetAngle = 135f + (targetPercentage / 100f) * 270f
                    val rad = Math.toRadians(targetAngle.toDouble())
                    val markerX = centerOffset.x + (radius * cos(rad)).toFloat()
                    val markerY = centerOffset.y + (radius * sin(rad)).toFloat()

                    drawCircle(
                        color = Color.White,
                        radius = 10.dp.toPx(),
                        center = Offset(markerX, markerY)
                    )
                    drawCircle(
                        color = if (isAlertEnabled) Color(0xFF00E676) else Color.Gray,
                        radius = 6.dp.toPx(),
                        center = Offset(markerX, markerY)
                    )
                }

                // Center Battery Percentage Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCharging) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(34.dp)
                                    .alpha(if (isCharging) pulseAlpha else 1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (batteryInfo.level >= 0) "${batteryInfo.level}%" else "—",
                            fontSize = 46.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCharging) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = if (isCharging) "⚡ ${batteryInfo.plugType}" else (if (isEn) "🔌 Plug In Charger" else "🔌 الشاحن غير متصل"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCharging) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status Badge
            if (isCharging && batteryInfo.level >= targetPercentage) {
                Surface(
                    color = Color(0xFF2E7D32),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) {
                                "Target level ($targetPercentage%) reached! Unplug charger now."
                            } else {
                                "تم الوصول إلى المستوى المستهدف ($targetPercentage%)! افصل الشاحن الآن."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEn) "Target Level" else "النسبة المستهدفة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$targetPercentage%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEn) "Health" else "حالة البطارية",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = batteryInfo.health,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TargetSettingCard(
    isEn: Boolean,
    targetPercentage: Int,
    currentLevel: Int,
    onTargetChanged: (Int) -> Unit
) {
    var draftTarget by remember(targetPercentage) { mutableStateOf(targetPercentage.toFloat()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Set Target Percentage" else "تحديد نسبة الشحن المستهدفة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${kotlin.math.round(draftTarget).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = draftTarget,
                onValueChange = { draftTarget = it },
                onValueChangeFinished = { onTargetChanged(kotlin.math.round(draftTarget).toInt()) },
                valueRange = 50f..100f,
                steps = 49,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("target_percentage_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "50%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isEn) "80% (Recommended)" else "80% (مستحسن لصحة البطارية)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "100%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isEn) "Quick Presets:" else "اختيارات سريعة:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = if (isEn) {
                    listOf(80 to "80% (Rec)", 85 to "85%", 90 to "90%", 100 to "100%")
                } else {
                    listOf(80 to "80% (موصى به)", 85 to "85%", 90 to "90%", 100 to "100%")
                }
                presets.forEach { (pct, label) ->
                    val isSelected = targetPercentage == pct
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTargetChanged(pct) },
                        label = { Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MasterToggleCard(
    isEn: Boolean,
    isAlertEnabled: Boolean,
    onAlertEnabledChanged: (Boolean) -> Unit
) {
    val bgGradient = if (isAlertEnabled) {
        Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF37474F), Color(0xFF455A64), Color(0xFF546E7A)))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgGradient, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAlertEnabled) Icons.Default.Power else Icons.Default.BatterySaver,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isAlertEnabled) {
                                if (isEn) "App Activated" else "التطبيق مُفعّل"
                            } else {
                                if (isEn) "App Deactivated" else "التطبيق مُعطّل"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAlertEnabled) {
                            if (isEn) "Monitoring is active. The app will alert you when charge target is reached."
                            else "المراقبة نشطة. سيُنبّهك التطبيق عند وصول الشحن للنسبة المستهدفة."
                        } else {
                            if (isEn) "Turn on to enable charge monitoring and alerts."
                            else "قم بتشغيل التطبيق لتفعيل مراقبة الشحن والتنبيهات."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    checked = isAlertEnabled,
                    onCheckedChange = onAlertEnabledChanged,
                    modifier = Modifier.testTag("master_app_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF78909C)
                    )
                )
            }
        }
    }
}

@Composable
fun AlertControlsCard(
    isEn: Boolean,
    alertSettings: AlertSettings,
    onAlertEnabledChanged: (Boolean) -> Unit,
    onSoundEnabledChanged: (Boolean) -> Unit,
    onVibrateEnabledChanged: (Boolean) -> Unit,
    onRepeatNotificationChanged: (Boolean) -> Unit,
    onTestNotification: () -> Unit,
    onSoundTypeSelected: (String) -> Unit = {},
    onPreviewSound: (String) -> Unit = {},
    onStopPreview: () -> Unit = {}
) {
    var isPlayingPreview by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // ── Section Header ───────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Alert & Sound Settings" else "إعدادات التنبيه والصوت",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Sound Toggle ─────────────────────────────────────────────
            SettingToggleRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = if (isEn) "Notification Sound" else "نغمة التنبيه الصوتي",
                subtitle = if (isEn) "Play alarm tone on target reached" else "تشغيل صوت إنذار عند اكتمال الشحن",
                checked = alertSettings.soundEnabled,
                enabled = alertSettings.isAlertEnabled,
                onCheckedChange = onSoundEnabledChanged,
                testTag = "sound_toggle_switch"
            )

            // ── Sound Type Selector (embedded inline) ────────────────────
            AnimatedVisibility(visible = alertSettings.soundEnabled && alertSettings.isAlertEnabled) {
                Column(modifier = Modifier.padding(start = 52.dp, top = 8.dp, bottom = 8.dp)) {
                    val soundOptions = if (isEn) {
                        listOf(
                            Triple("ALARM", "🚨 Alarm Siren", "Loud alarm (Recommended)"),
                            Triple("NOTIFICATION", "🔔 Chime", "Short notification"),
                            Triple("RINGTONE", "📞 Ringtone", "Phone ringtone")
                        )
                    } else {
                        listOf(
                            Triple("ALARM", "🚨 منبه (Alarm)", "صوت إنذار قوي (مستحسن)"),
                            Triple("NOTIFICATION", "🔔 إشعار (Notification)", "نغمة إشعار قصيرة"),
                            Triple("RINGTONE", "📞 رنين (Ringtone)", "نغمة المكالمات")
                        )
                    }

                    soundOptions.forEach { (typeKey, title, desc) ->
                        val isSelected = alertSettings.soundType.equals(typeKey, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSoundTypeSelected(typeKey) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preview button
                    OutlinedButton(
                        onClick = {
                            if (isPlayingPreview) {
                                onStopPreview()
                                isPlayingPreview = false
                            } else {
                                onPreviewSound(alertSettings.soundType)
                                isPlayingPreview = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingPreview) Icons.Default.Vibration else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingPreview) {
                                if (isEn) "⏹ Stop" else "⏹ إيقاف"
                            } else {
                                if (isEn) "🔊 Preview Tone" else "🔊 معاينة النغمة"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Vibration Toggle ──────────────────────────────────────────
            SettingToggleRow(
                icon = Icons.Default.Vibration,
                title = if (isEn) "Vibration Alert" else "الاهتزاز عند التنبيه",
                subtitle = if (isEn) "Vibrate phone on target reached" else "اهتزاز الهاتف عند وصول مستوى الشحن",
                checked = alertSettings.vibrateEnabled,
                enabled = alertSettings.isAlertEnabled,
                onCheckedChange = onVibrateEnabledChanged,
                testTag = "vibrate_toggle_switch"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Repeat Toggle ────────────────────────────────────────────
            SettingToggleRow(
                icon = Icons.Default.Repeat,
                title = if (isEn) "Repeat reminder" else "تكرار التذكير",
                subtitle = if (isEn) "Every 5 minutes until dismissed. Sound stops after 1 minute." else "كل ٥ دقائق حتى إيقافه. يتوقف الصوت بعد دقيقة.",
                checked = alertSettings.repeatNotification,
                enabled = alertSettings.isAlertEnabled,
                onCheckedChange = onRepeatNotificationChanged,
                testTag = "repeat_notification_toggle_switch"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Test Button ──────────────────────────────────────────────
            Button(
                onClick = onTestNotification,
                enabled = alertSettings.isAlertEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("test_notification_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Test Alert Notification Now" else "تجربة إشعار التنبيه الآن",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}


@Composable
fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}


@Composable
fun BatteryOptimizationCard(
    isEn: Boolean,
    onRequestOptimizationExemption: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Battery Optimization Exemption" else "استثناء التثبيط في الخلفية (Battery Optimization)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (isEn) "Allow Charge Alert to run in background unthrottled for reliable alerts during screen sleep." else "لضمان وصول التنبيه أثناء نوم الشاشة، اسمح للتطبيق بالعمل في الخلفية دون قيود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onRequestOptimizationExemption,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(if (isEn) "Allow Unrestricted" else "السماح بالعمل دون قيود", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onRefreshStatus,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isEn) "Refresh" else "تحديث الحالة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenIntentPermissionCard(isEn: Boolean, onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Full-Screen Intent Permission Required" else "صلاحية شاشة القفل الكاملة مطلوبة",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = if (isEn) "Grant full-screen intent permission so the lockscreen alert can appear over the lockscreen on Android 14+." else "امنح صلاحية شاشة القفل الكاملة لتظهر شاشة التنبيه فوق شاشة القفل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(if (isEn) "Grant Full-Screen Permission" else "منح صلاحية شاشة القفل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OemAutoStartCard(
    isEn: Boolean,
    onOpenSettings: () -> Unit,
    onDismissCard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Auto-Start Permission Required" else "صلاحية التشغيل التلقائي (Auto-Start)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = if (isEn) "Your device requires auto-start permission for background monitoring to work reliably after reboot." else "جهازك يحتاج إلى إذن التشغيل التلقائي لضمان عمل المراقبة في الخلفية بعد إعادة التشغيل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenSettings,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(if (isEn) "Open Settings" else "فتح الإعدادات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDismissCard,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isEn) "✓ Configured (Hide)" else "✓ تم التفعيل (إخفاء البطاقة)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionCard(
    isEn: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Notification Permission Required" else "الموافقة على إذن الإشعارات مطلوبة",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = if (isEn) "Grant notification permission so Charge Alert can inform you on target reached." else "يرجى منح إذن الإشعارات ليتمكن التطبيق من إشعارك عند اكتمال نسبة الشحن.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onRequestPermission,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(if (isEn) "Grant Permission" else "منح الإذن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onOpenAppSettings,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isEn) "App Settings" else "إعدادات التطبيق", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryMetricsCard(isEn: Boolean, batteryInfo: BatteryInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Battery Status & Hardware" else "حالة البطارية وعتاد الجهاز",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Thermostat,
                    title = if (isEn) "Temperature" else "درجة الحرارة",
                    value = if (batteryInfo.temperature.isFinite()) "${batteryInfo.temperature}°C" else "—"
                )

                MetricItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Power,
                    title = if (isEn) "Voltage" else "فرق الجهد",
                    value = "${batteryInfo.voltage} mV"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    title = if (isEn) "Health" else "جودة البطارية",
                    value = batteryInfo.health
                )

                MetricItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BatteryChargingFull,
                    title = if (isEn) "Technology" else "تقنية التصنيع",
                    value = batteryInfo.technology
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DeveloperFooterCard(isEn: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            if (isEn) "Developed by Ahmad Ibrahim" else "تطوير أحمد إبراهيم",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "ahmadibrahim.geo@gmail.com",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// App Status Card — answers "is the app ready and working?"
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppStatusCard(
    isEn: Boolean,
    isAlertEnabled: Boolean,
    isCharging: Boolean,
    monitoringActive: Boolean = false,
    onResumeMonitoring: () -> Unit = {},
    targetPercentage: Int,
    hasNotificationPermission: Boolean,
    isIgnoringBatteryOptimizations: Boolean,
    canUseFullScreenIntent: Boolean = true
) {
    // Determine overall app state
    val permissionsOk = hasNotificationPermission && isIgnoringBatteryOptimizations

    val state = when {
        !isAlertEnabled            -> AppState.DISABLED
        !permissionsOk             -> AppState.NEEDS_SETUP
        isCharging && !monitoringActive -> AppState.DISABLED
        isCharging                 -> AppState.MONITORING
        else                       -> AppState.READY
    }

    val inf = androidx.compose.animation.core.rememberInfiniteTransition(label = "status")
    val pulse by inf.animateFloat(
        0.4f, 1f,
        infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val cardColor = when (state) {
        AppState.MONITORING  -> Color(0xFF00381C)
        AppState.READY       -> Color(0xFF002240)
        AppState.NEEDS_SETUP -> Color(0xFF2D1B00)
        AppState.DISABLED    -> Color(0xFF1A1A1A)
    }
    val borderColor = when (state) {
        AppState.MONITORING  -> Color(0xFF00E676)
        AppState.READY       -> Color(0xFF00B0FF)
        AppState.NEEDS_SETUP -> Color(0xFFFFAB00)
        AppState.DISABLED    -> Color(0xFF555555)
    }
    val dotColor = borderColor

    val title = when (state) {
        AppState.MONITORING  -> if (isEn) "🟢 Monitoring Active" else "🟢 المراقبة نشطة الآن"
        AppState.READY       -> if (isEn) "✅ App Ready" else "✅ التطبيق جاهز"
        AppState.NEEDS_SETUP -> if (isEn) "⚠️ Setup Needed" else "⚠️ يحتاج إعداداً"
        AppState.DISABLED    -> if (isAlertEnabled) { if (isEn) "⏸ Monitoring paused" else "⏸ المراقبة متوقفة" }
            else if (isEn) "⏸ Alert Disabled" else "⏸ التنبيه معطّل"
    }

    val subtitle = when (state) {
        AppState.MONITORING -> if (isEn)
            "Charging in progress — alert fires at $targetPercentage%"
        else
            "الجهاز يُشحن الآن — سيتنبّه عند بلوغ $targetPercentage%"

        AppState.READY -> if (isEn)
            "Waiting for a charger. Background recovery timing depends on Android."
        else
            "بانتظار توصيل الشاحن. توقيت الاستعادة في الخلفية يخضع لأندرويد."

        AppState.NEEDS_SETUP -> if (isEn)
            "Grant the required permissions above so the app can alert you reliably"
        else
            "امنح الصلاحيات المطلوبة أعلاه حتى يعمل التطبيق بشكل موثوق"

        AppState.DISABLED -> if (isEn)
            "Enable the alert toggle in the settings below to activate"
        else
            "فعّل مفتاح التنبيه في الإعدادات أدناه للتشغيل"
    }

    val steps = if (isEn) listOf(
        "1" to "Open the app & configure your settings",
        "2" to "Grant notification & battery permissions",
        "3" to "Choose whether to keep standby monitoring enabled",
        "4" to "Connect the charger; open the app if monitoring is delayed",
        "5" to "Alert fires when battery hits your target ($targetPercentage%)"
    ) else listOf(
        "1" to "افتح التطبيق وحدد إعداداتك",
        "2" to "امنح أذونات الإشعارات والبطارية",
        "3" to "اختر تفعيل المراقبة الدائمة أو إيقافها",
        "4" to "وصّل الشاحن وافتح التطبيق إذا تأخر بدء المراقبة",
        "5" to "ينبّهك فور الوصول لـ $targetPercentage%"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = cardColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor.copy(if (state == AppState.MONITORING) pulse else 0.7f))
    ) {
        Column(Modifier.padding(20.dp)) {

            // Title row with live indicator dot
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(
                            dotColor.copy(alpha = if (state == AppState.MONITORING) pulse else 1f),
                            CircleShape
                        )
                )
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.72f), lineHeight = 18.sp)

            if (isAlertEnabled && isCharging && !monitoringActive && hasNotificationPermission) {
                OutlinedButton(onClick = onResumeMonitoring) {
                    Text(if (isEn) "Resume monitoring" else "استئناف المراقبة", color = Color.White)
                }
            }

            // Permission status pills
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Notifications Badge
                val notifOk = hasNotificationPermission
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (notifOk) Color(0xFF00E676).copy(0.15f) else Color(0xFFFF5252).copy(0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (notifOk) Color(0xFF00E676).copy(0.4f) else Color(0xFFFF5252).copy(0.4f))
                ) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (notifOk) "✓" else "✕", color = if (notifOk) Color(0xFF00E676) else Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isEn) "Notifications" else "الإشعارات", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Battery Optimization Badge
                val batOk = isIgnoringBatteryOptimizations
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (batOk) Color(0xFF00E676).copy(0.15f) else Color(0xFFFFAB00).copy(0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (batOk) Color(0xFF00E676).copy(0.4f) else Color(0xFFFFAB00).copy(0.4f))
                ) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (batOk) "✓" else "✕", color = if (batOk) Color(0xFF00E676) else Color(0xFFFFAB00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isEn) "Background" else "الخلفية", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Lockscreen Badge
                val fsiOk = canUseFullScreenIntent
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (fsiOk) Color(0xFF00E676).copy(0.15f) else Color(0xFF00B0FF).copy(0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (fsiOk) Color(0xFF00E676).copy(0.4f) else Color(0xFF00B0FF).copy(0.4f))
                ) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (fsiOk) "✓" else "ℹ", color = if (fsiOk) Color(0xFF00E676) else Color(0xFF00B0FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isEn) "Lockscreen" else "شاشة القفل", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Show how-it-works steps only when not actively monitoring
            if (state != AppState.MONITORING) {
                Spacer(Modifier.height(16.dp))
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = Color.Black.copy(0.25f)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (isEn) "How it works:" else "كيف يعمل التطبيق:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = borderColor,
                            letterSpacing = 0.5.sp
                        )
                        steps.forEach { (num, text) ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    Modifier
                                        .size(20.dp)
                                        .background(borderColor.copy(0.18f), CircleShape)
                                        .border(0.5.dp, borderColor.copy(0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(num, fontSize = 10.sp, fontWeight = FontWeight.Black, color = borderColor)
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(text, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.82f), modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class AppState { MONITORING, READY, NEEDS_SETUP, DISABLED }
