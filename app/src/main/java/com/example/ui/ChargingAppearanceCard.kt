package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertSettings
import com.example.model.BatteryInfo
import com.example.ui.theme.*

@Composable
fun ChargingAppearanceCard(
    isEn: Boolean, settings: AlertSettings, onEnabledChanged: (Boolean) -> Unit,
    onThemeSelected: (String) -> Unit, onPreview: () -> Unit
) {
    val theme = findChargingTheme(settings.lockscreenThemeId)
    val powerSave = (LocalContext.current.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager).isPowerSaveMode
    var collection by remember { mutableStateOf<ThemeCollection?>(null) }
    val designs = LOCKSCREEN_THEMES_LIST.filter { collection == null || it.collection == collection }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (isEn) "Your charging style" else "الشحن على ذوقك",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if (isEn) "20 designs · 10 new additions" else "٢٠ تصميم · ١٠ أنماط جديدة",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.isLockscreenEnabled, onCheckedChange = onEnabledChanged)
            }
            Surface(shape = RoundedCornerShape(26.dp), color = theme.backgroundColor,
                border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.25f))) {
                Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(
                    listOf(theme.backgroundColor, Color(0xFF060A12)))).padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isEn) theme.nameEn else theme.nameAr, color = Color(0xFFF2F4FA),
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Surface(color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(50)) {
                            Text(if (isEn) "SAMPLE 76%" else "معاينة 76%", color = theme.accentColor,
                                fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                    key(theme.id) {
                        RenderLockscreenTheme(theme.id, BatteryInfo(level = 76, isCharging = true), settings.targetPercentage,
                            false, modifier = Modifier.widthIn(max = 300.dp).fillMaxWidth().aspectRatio(1f),
                            animated = settings.animationsEnabled && !powerSave)
                    }
                    Text(if (isEn) theme.descriptionEn else theme.descriptionAr,
                        color = Color(0xFFB3BECD), fontSize = 12.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onPreview, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                        shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(
                            containerColor = theme.accentColor, contentColor = Color(0xFF09141B))) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEn) "Preview full charging screen" else "معاينة شاشة الشحن كاملة", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(if (isEn) "Find your favourite" else "اختار الشكل الأقرب لك",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = collection == null, onClick = { collection = null },
                        label = { Text(if (isEn) "All · 20" else "الكل · ٢٠") })
                }
                items(ThemeCollection.entries) { group ->
                    FilterChip(selected = collection == group, onClick = { collection = group },
                        label = { Text(if (isEn) group.nameEn else group.nameAr) })
                }
            }
            designs.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { design ->
                        ChargingThemeTile(design, settings.lockscreenThemeId == design.id, isEn,
                            onClick = { onThemeSelected(design.id) }, modifier = Modifier.weight(1f))
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Text(if (isEn) "Gallery uses sample data. Full-screen preview uses your actual battery reading."
                else "المعرض ببيانات توضيحية. المعاينة الكاملة تعرض قراءة بطاريتك الفعلية.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ChargingThemeTile(
    design: ThemeDescriptor, isSelected: Boolean, isEn: Boolean, onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(onClick = onClick,
        modifier = modifier.testTag("theme_card_${design.id}").semantics { selected = isSelected },
        shape = RoundedCornerShape(20.dp), color = design.backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp,
            if (isSelected) design.accentColor else Color.White.copy(alpha = 0.08f))) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(design.backgroundColor, Color(0xFF080C14)))),
                contentAlignment = Alignment.Center) {
                RenderLockscreenTheme(design.id, BatteryInfo(level = 76, isCharging = true), 80, false,
                    modifier = Modifier.fillMaxSize(), animated = false, showLabels = true)
                if (design.isNew) Surface(modifier = Modifier.align(Alignment.TopStart),
                    shape = RoundedCornerShape(50), color = Color(0xFF111B25)) {
                    Text(if (isEn) "NEW" else "جديد", fontSize = 9.sp, color = design.accentColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (isEn) design.nameEn else design.nameAr, color = Color(0xFFE9EEF5),
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 2)
                if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = if (isEn) "Selected" else "مختار",
                    tint = design.accentColor, modifier = Modifier.size(17.dp))
            }
            Text(if (isEn) design.collection.nameEn else design.collection.nameAr,
                color = Color(0xFF98A6B8), fontSize = 10.sp)
        }
    }
}
