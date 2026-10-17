package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.model.BatteryInfo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

enum class ThemeCollection(val nameAr: String, val nameEn: String) {
    LIGHT("ضوء وطاقة", "Light"), NATURE("من الطبيعة", "Nature"), STUDIO("هادئ وأنيق", "Studio")
}

data class ThemeDescriptor(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val accentColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val collection: ThemeCollection,
    val isNew: Boolean = false
)

// Keep the original ten IDs so saved selections continue to resolve after upgrading.
val LOCKSCREEN_THEMES_LIST = listOf(
    ThemeDescriptor("NEON_RING", "هالة النيون", "Neon Halo", "قوس للشحن الحالي وعلامة بيضاء للهدف", "Current charge arc with a white target mark", Color(0xFF5BE7F3), Color(0xFF4985FF), Color(0xFF071723), ThemeCollection.LIGHT),
    ThemeDescriptor("MINIMAL_GLASS", "الزجاج المنشوري", "Prism Glass", "طبقات زجاجية وانعكاسات لؤلؤية", "Layered glass with pearlescent reflections", Color(0xFFB8E8FF), Color(0xFFB5A2FF), Color(0xFF111A2C), ThemeCollection.STUDIO),
    ThemeDescriptor("HOLOGRAM_WAVE", "المدّ البنفسجي", "Violet Tide", "سائل مضيء يرتفع مع الشحن", "Luminous liquid follows your battery", Color(0xFFAA99FF), Color(0xFF5BDDD0), Color(0xFF16122D), ThemeCollection.NATURE),
    ThemeDescriptor("ARC_REACTOR", "نواة الطاقة", "Energy Core", "حلقات هندسية بنبض هادئ", "Precision geometry with a gentle pulse", Color(0xFF84E5E1), Color(0xFF4B93C4), Color(0xFF081C22), ThemeCollection.LIGHT),
    ThemeDescriptor("MATRIX_PULSE", "الدوائر الذكية", "Circuit Studio", "مسارات إلكترونية بتفاصيل نظيفة", "Clean circuitry around a central chip", Color(0xFF9BE8BC), Color(0xFF3D9D87), Color(0xFF0B201A), ThemeCollection.STUDIO),
    ThemeDescriptor("ULTRA_BAR", "أعمدة الطاقة", "Energy Columns", "أعمدة ناعمة تعكس النسبة", "Soft columns map your charge level", Color(0xFFC3ED9D), Color(0xFF63C5A3), Color(0xFF172116), ThemeCollection.STUDIO),
    ThemeDescriptor("DIGITAL_CLOCK", "ساعة منتصف الليل", "Midnight Clock", "الوقت والشحن في لوحة هادئة", "Time and charge in a quiet composition", Color(0xFFC2D4F0), Color(0xFF7E95C2), Color(0xFF111928), ThemeCollection.STUDIO),
    ThemeDescriptor("ENERGY_SPHERE", "مدارات البلازما", "Plasma Orbits", "مدارات متقاطعة من الضوء", "Intersecting elliptical light orbits", Color(0xFFD0ADFF), Color(0xFF837BFF), Color(0xFF1B102F), ThemeCollection.LIGHT),
    ThemeDescriptor("GRADIENT_GLOW", "ستائر الشفق", "Aurora Curtains", "شرائط شفقية خضراء وبنفسجية", "Flowing green and violet aurora", Color(0xFF87EBD3), Color(0xFFAA88ED), Color(0xFF0B222B), ThemeCollection.NATURE),
    ThemeDescriptor("SOLAR_FLARE", "التاج الشمسي", "Solar Corona", "تاج دافئ وأشعة دقيقة", "A warm corona with fine solar rays", Color(0xFFFFC67A), Color(0xFFEF886F), Color(0xFF281A14), ThemeCollection.LIGHT),
    ThemeDescriptor("LUNAR_ECLIPSE", "خسوف القمر", "Lunar Eclipse", "هلال فضي في سماء ليلية", "A silver crescent in a midnight sky", Color(0xFFD5DFF5), Color(0xFF7D8FB8), Color(0xFF101729), ThemeCollection.NATURE, true),
    ThemeDescriptor("OCEAN_DEPTH", "عمق المحيط", "Ocean Depth", "أمواج زرقاء بمستويات هادئة", "Quiet layers of deep blue waves", Color(0xFF80D8EC), Color(0xFF478AB5), Color(0xFF082133), ThemeCollection.NATURE, true),
    ThemeDescriptor("ROSE_SILK", "حرير وردي", "Rose Silk", "شرائط حريرية تنساب برقة", "Flowing ribbons of rose-tinted silk", Color(0xFFF3B5CF), Color(0xFFB384D9), Color(0xFF281725), ThemeCollection.STUDIO, true),
    ThemeDescriptor("GOLDEN_DIAL", "الميناء الذهبي", "Golden Dial", "تدرّج نحاسي وتفاصيل ساعة فاخرة", "Brushed gold and watch-like detailing", Color(0xFFE5C48D), Color(0xFFAD8760), Color(0xFF211D17), ThemeCollection.STUDIO, true),
    ThemeDescriptor("MONO_STUDIO", "استوديو أبيض", "Mono Studio", "طباعة جريئة بتباين أبيض وأسود", "Bold typography in monochrome", Color(0xFFE8EDF3), Color(0xFF919AA7), Color(0xFF161A20), ThemeCollection.STUDIO, true),
    ThemeDescriptor("CRYSTAL_FACETS", "أوجه الكريستال", "Crystal Facets", "بلورة مضيئة بأوجه متقاطعة", "A luminous gem with intersecting facets", Color(0xFFB8CCFF), Color(0xFFA88DDC), Color(0xFF131B35), ThemeCollection.LIGHT, true),
    ThemeDescriptor("BLOOM", "زهرة الضوء", "Light Bloom", "بتلات ضوئية تحيط بنسبة الشحن", "Luminous petals frame your charge", Color(0xFFF4BDCE), Color(0xFFD995AD), Color(0xFF291925), ThemeCollection.NATURE, true),
    ThemeDescriptor("TOPOGRAPHIC", "خطوط التضاريس", "Contour Lines", "طبقات عضوية بلون المريمية", "Organic contours in soft sage", Color(0xFFB7D4AB), Color(0xFF718D80), Color(0xFF18231E), ThemeCollection.NATURE, true),
    ThemeDescriptor("COMET_TRAIL", "مسار المذنب", "Comet Trail", "مذنب مضيء يعبر مجال النجوم", "A luminous comet in a field of stars", Color(0xFFADC9FF), Color(0xFFBD9BEA), Color(0xFF11172D), ThemeCollection.LIGHT, true),
    ThemeDescriptor("SUNSET_HORIZON", "أفق الغروب", "Sunset Horizon", "شمس دافئة وتلال بنفسجية", "A warm sun over violet hills", Color(0xFFFFCA99), Color(0xFFC595CA), Color(0xFF2C1C2A), ThemeCollection.NATURE, true)
)

fun findChargingTheme(id: String): ThemeDescriptor =
    LOCKSCREEN_THEMES_LIST.firstOrNull { it.id == id } ?: LOCKSCREEN_THEMES_LIST.first()

private val movingThemes = setOf("NEON_RING", "HOLOGRAM_WAVE", "ARC_REACTOR", "MATRIX_PULSE",
    "ENERGY_SPHERE", "GRADIENT_GLOW", "SOLAR_FLARE", "LUNAR_ECLIPSE", "OCEAN_DEPTH",
    "ROSE_SILK", "BLOOM", "TOPOGRAPHIC", "COMET_TRAIL")

fun hasChargingAnimation(themeId: String): Boolean = findChargingTheme(themeId).id in movingThemes

/** One bounded animation clock for the selected design. Thumbnails and reduced motion are static. */
@Composable
fun RenderLockscreenTheme(
    themeId: String,
    batteryInfo: BatteryInfo,
    targetPct: Int,
    isTargetReached: Boolean,
    modifier: Modifier = Modifier.size(300.dp),
    animated: Boolean = true,
    showLabels: Boolean = true
) {
    val theme = findChargingTheme(themeId)
    val phase: State<Float> = if (animated && hasChargingAnimation(theme.id) &&
        (theme.id != "NEON_RING" || batteryInfo.isCharging)) {
        val transition = rememberInfiniteTransition(label = "charging_art")
        transition.animateFloat(0f, 1f,
            infiniteRepeatable(tween(24_000, easing = LinearEasing)), label = "art_phase")
    } else remember { mutableStateOf(0.18f) }
    val en = LocalLayoutDirection.current == LayoutDirection.Ltr
    val levelText = if (batteryInfo.level in 0..100) "${batteryInfo.level}%" else "—"
    BoxWithConstraints(modifier.semantics {
        contentDescription = if (en) "${theme.nameEn}, battery $levelText" else "${theme.nameAr}، البطارية $levelText"
        stateDescription = if (en) "Current charge $levelText. Target $targetPct%."
            else "الشحن الحالي $levelText. الهدف $targetPct%."
        progressBarRangeInfo = if (batteryInfo.level in 0..100)
            ProgressBarRangeInfo(batteryInfo.level / 100f, 0f..1f) else ProgressBarRangeInfo.Indeterminate
    }, contentAlignment = Alignment.Center) {
        val ratio = maxWidth.value / 300f
        Canvas(Modifier.fillMaxSize()) {
            withTransform({ scale(size.width / 300f, size.height / 300f, Offset.Zero) }) {
                drawChargingArt(theme, batteryInfo.level.coerceIn(0, 100) / 100f,
                    targetPct.coerceIn(0, 100) / 100f, phase.value, isTargetReached,
                    showChargingPulse = animated && batteryInfo.isCharging && batteryInfo.level in 0..100)
            }
        }
        if (showLabels) {
            val topDisplay = theme.id in listOf("ULTRA_BAR", "OCEAN_DEPTH", "ROSE_SILK")
            val alignment = when {
                theme.id == "MONO_STUDIO" -> Alignment.CenterStart
                topDisplay -> Alignment.TopCenter
                else -> Alignment.Center
            }
            Column(Modifier.align(alignment)
                .padding(top = if (topDisplay) (34 * ratio).dp else 0.dp,
                    start = if (theme.id == "MONO_STUDIO") (32 * ratio).dp else 0.dp),
                horizontalAlignment = if (theme.id == "MONO_STUDIO") Alignment.Start else Alignment.CenterHorizontally) {
                if (theme.id == "DIGITAL_CLOCK") {
                    var time by remember { mutableStateOf(clockText()) }
                    LaunchedEffect(Unit) {
                        while (true) { delay(60_000); time = clockText() }
                    }
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(time, color = Color(0xFFF0F3FF), fontSize = (48 * ratio).sp,
                            fontWeight = FontWeight.Light, letterSpacing = (2 * ratio).sp)
                    }
                    Text(levelText, color = theme.accentColor, fontSize = (26 * ratio).sp, fontWeight = FontWeight.Medium)
                } else {
                    if (theme.id == "NEON_RING") {
                        Text(if (en) "CURRENT CHARGE" else "الشحن الحالي", color = Color(0xFFB3C6D1),
                            fontSize = (11 * ratio).sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height((4 * ratio).dp))
                    }
                    Text(levelText, color = Color(0xFFF3F5FA), fontSize = ((if (theme.id == "MONO_STUDIO") 72 else 58) * ratio).sp,
                        fontWeight = if (theme.collection == ThemeCollection.STUDIO) FontWeight.Light else FontWeight.SemiBold,
                        fontFamily = if (theme.id == "MATRIX_PULSE") FontFamily.Monospace else FontFamily.SansSerif,
                        letterSpacing = (-2 * ratio).sp, maxLines = 1)
                }
                Spacer(Modifier.height((5 * ratio).dp))
                Text(if (isTargetReached) { if (en) "TARGET REACHED" else "اكتمل الهدف" }
                    else if (en) "TARGET $targetPct%" else "الهدف $targetPct%",
                    color = theme.accentColor.copy(alpha = 0.9f), fontSize = (11 * ratio).sp,
                    fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            }
        }
    }
}

private fun clockText() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
private val origin = Offset(150f, 150f)
private fun point(radius: Float, degrees: Float, center: Offset = origin): Offset {
    val angle = degrees * PI.toFloat() / 180f
    return center + Offset(cos(angle) * radius, sin(angle) * radius)
}
private fun polygon(radius: Float, count: Int, rotation: Float = -90f, center: Offset = origin) = Path().apply {
    for (i in 0 until count) {
        val p = point(radius, rotation + 360f * i / count, center)
        if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
    }
    close()
}
private fun DrawScope.ring(radius: Float, color: Color, width: Float = 1f, start: Float = 0f, sweep: Float = 360f) {
    drawArc(color, start, sweep, false, Offset(150 - radius, 150 - radius), Size(radius * 2, radius * 2),
        style = Stroke(width, cap = StrokeCap.Round))
}
private fun DrawScope.glow(at: Offset, radius: Float, color: Color, alpha: Float = 0.3f) {
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = alpha), Color.Transparent), at, radius), radius, at)
}
private fun DrawScope.targetDot(target: Float, radius: Float, start: Float = 135f, sweep: Float = 270f) {
    val at = point(radius, start + sweep * target)
    drawCircle(Color(0xFF08121A), 5f, at)
    drawCircle(Color(0xFFF2F2EE), 2.8f, at)
}

private fun DrawScope.drawChargingArt(t: ThemeDescriptor, level: Float, target: Float, phase: Float,
    reached: Boolean, showChargingPulse: Boolean) {
    val a = t.accentColor
    val b = t.secondaryColor
    val breath = 0.5f + 0.5f * sin(phase * 2 * PI.toFloat())
    when (t.id) {
        "NEON_RING" -> {
            // Only this filled arc encodes progress. The track represents the rest to 100%.
            ring(108f, Color(0xFF263B49), 10f, 135f, 270f)
            if (level > 0f) ring(108f, a, 10f, 135f, 270f * level)
            val current = point(108f, 135f + 270f * level)
            if (showChargingPulse) {
                val pulse = 0.5f + 0.5f * sin(phase * 24f * PI.toFloat())
                glow(current, 13f + 9f * pulse, a, 0.18f + 0.35f * pulse)
            }
            // A fixed outward white tick, separated from the current-charge endpoint.
            val goalAngle = 135f + 270f * target
            drawLine(Color(0xFFF1F5F9), point(118f, goalAngle), point(131f, goalAngle),
                3f, StrokeCap.Round)
            drawCircle(a, 3.5f, current)
        }
        "MINIMAL_GLASS" -> {
            glow(Offset(80f, 105f), 125f, b, 0.28f)
            glow(Offset(230f, 205f), 105f, a, 0.22f)
            rotate(-10f, pivot = origin) {
                drawRoundRect(b.copy(alpha = 0.07f), Offset(35f, 65f), Size(230f, 166f), CornerRadius(26f))
                drawRoundRect(b.copy(alpha = 0.25f), Offset(35f, 65f), Size(230f, 166f), CornerRadius(26f), style = Stroke(1f))
            }
            rotate(6f, pivot = origin) {
                drawRoundRect(a.copy(alpha = 0.04f), Offset(30f, 63f), Size(240f, 174f), CornerRadius(26f), style = Stroke(1f))
            }
            drawRoundRect(diagonal(listOf(Color(0xFF26364D).copy(alpha = 0.92f), Color(0xFF121D31).copy(alpha = 0.96f))),
                Offset(23f, 70f), Size(254f, 160f), CornerRadius(24f))
            drawRoundRect(diagonal(listOf(a.copy(alpha = 0.75f), Color.Transparent, b.copy(alpha = 0.5f))),
                Offset(23f, 70f), Size(254f, 160f), CornerRadius(24f), style = Stroke(1.2f))
            drawLine(a.copy(alpha = 0.6f), Offset(44f, 91f), Offset(89f, 91f), 1.5f)
            drawLine(b.copy(alpha = 0.55f), Offset(210f, 208f), Offset(254f, 208f), 1.5f)
            drawRoundRect(Color.White.copy(alpha = 0.08f), Offset(48f, 210f), Size(132f, 3f), CornerRadius(2f))
            drawRoundRect(a.copy(alpha = 0.8f), Offset(48f, 210f), Size(132f * level, 3f), CornerRadius(2f))
        }
        "HOLOGRAM_WAVE" -> {
            glow(origin, 142f, b, 0.13f)
            val orb = Path().apply { addOval(androidx.compose.ui.geometry.Rect(35f, 35f, 265f, 265f)) }
            clipPath(orb) {
                drawCircle(Color(0xFF191D38), 115f, origin)
                repeat(3) { layer ->
                    val y = 252f - level * 195f + layer * 12f
                    val wave = Path().apply {
                        moveTo(25f, 280f); lineTo(25f, y)
                        for (x in 25..275 step 3) {
                            lineTo(x.toFloat(), y + sin(x / 49f + phase * 2 * PI.toFloat() + layer) * 10f)
                        }
                        lineTo(275f, 280f); close()
                    }
                    drawPath(wave, vertical(listOf((if (layer == 1) b else a).copy(alpha = 0.26f), Color(0xFF23254A))))
                }
                glow(Offset(210f, 60f), 100f, a, 0.25f)
                // A dark central lens keeps the reading clear at any fluid level.
                glow(origin, 80f, Color(0xFF080D21), 0.9f)
            }
            ring(115f, a.copy(alpha = 0.5f), 1f)
            ring(122f, b.copy(alpha = 0.25f), 1f, 210f, 100f)
        }
        "ARC_REACTOR" -> {
            glow(origin, 142f, a, 0.1f)
            rotate(phase * 60f, pivot = origin) {
                drawPath(polygon(130f, 6), a.copy(alpha = 0.3f), style = Stroke(1f))
                drawPath(polygon(118f, 6), b.copy(alpha = 0.25f), style = Stroke(1f))
            }
            repeat(6) { i ->
                ring(104f, a.copy(alpha = 0.18f), 9f, i * 60f - 87f, 43f)
                ring(104f, a, 3f, i * 60f - 87f, 43f * (level * 6 - i).coerceIn(0f, 1f))
            }
            drawPath(polygon(85f, 6), Color(0xFF0C242B))
            drawPath(polygon(85f, 6), a.copy(alpha = 0.25f + breath * 0.25f), style = Stroke(1f))
            repeat(6) { drawCircle(a.copy(alpha = 0.7f), 2f, point(130f, -90f + it * 60)) }
        }
        "MATRIX_PULSE" -> {
            repeat(6) { i ->
                val x = 62f + i * 35f
                val y = 36f + (i % 3) * 13f
                val path = Path().apply { moveTo(x, y); lineTo(x, 63f); lineTo(104f + i * 18f, 86f) }
                drawPath(path, a.copy(alpha = 0.28f), style = Stroke(1.1f))
                rotate(180f, pivot = origin) { drawPath(path, a.copy(alpha = 0.28f), style = Stroke(1.1f)) }
                drawCircle(a.copy(alpha = 0.7f), 2.5f, Offset(x, y))
                rotate(180f, pivot = origin) { drawCircle(a.copy(alpha = 0.7f), 2.5f, Offset(x, y)) }
            }
            repeat(4) { i ->
                val y = 96f + i * 36f
                drawLine(a.copy(alpha = 0.3f), Offset(25f, y), Offset(74f, y), 1f)
                drawLine(a.copy(alpha = 0.3f), Offset(226f, y), Offset(275f, y), 1f)
                drawCircle(a, 2f, Offset(25f + ((phase * 90 + i * 11) % 49f), y))
            }
            glow(origin, 108f, a, 0.1f)
            drawRoundRect(Color(0xFF102A23), Offset(76f, 76f), Size(148f, 148f), CornerRadius(25f))
            drawRoundRect(a.copy(alpha = 0.65f), Offset(76f, 76f), Size(148f, 148f), CornerRadius(25f), style = Stroke(1f))
            drawRoundRect(a.copy(alpha = 0.14f), Offset(84f, 84f), Size(132f, 132f), CornerRadius(19f), style = Stroke(1f))
            drawLine(a, Offset(93f, 211f), Offset(93f + 114f * level, 211f), 2f, StrokeCap.Round)
        }
        "ULTRA_BAR" -> {
            repeat(12) { i ->
                val x = 30f + i * 20f
                val height = 52f + sin(i / 11f * PI.toFloat()) * 38f
                drawRoundRect(a.copy(alpha = 0.07f), Offset(x, 235f - height), Size(13f, height), CornerRadius(6.5f))
                val fill = (level * 12f - i).coerceIn(0f, 1f)
                if (fill > 0f) {
                    drawRoundRect(vertical(listOf(a.copy(alpha = 0.9f), b.copy(alpha = 0.4f))),
                        Offset(x, 235f - height * fill), Size(13f, height * fill), CornerRadius(6.5f))
                }
            }
            drawLine(a.copy(alpha = 0.15f), Offset(30f, 253f), Offset(263f, 253f), 1f)
            drawCircle(Color.White, 2f, Offset(30f + 233f * target, 253f))
        }
        "DIGITAL_CLOCK" -> {
            glow(Offset(150f, 60f), 125f, b, 0.1f)
            drawRoundRect(a.copy(alpha = 0.08f), Offset(21f, 62f), Size(258f, 176f), CornerRadius(34f), style = Stroke(1f))
            repeat(12) { i ->
                val x = 43f + i * 19.5f
                drawLine(a.copy(alpha = if (i % 3 == 0) 0.5f else 0.15f), Offset(x, 85f), Offset(x, if (i % 3 == 0) 91f else 88f), 1f)
            }
            drawRoundRect(a.copy(alpha = 0.14f), Offset(48f, 218f), Size(204f, 2f), CornerRadius(1f))
            drawRoundRect(a.copy(alpha = 0.8f), Offset(48f, 218f), Size(204f * level, 2f), CornerRadius(1f))
            drawCircle(Color.White, 2.5f, Offset(48f + 204f * target, 218f))
        }
        "ENERGY_SPHERE" -> {
            glow(origin, 138f, b, 0.14f)
            repeat(3) { i ->
                rotate(i * 60f + phase * 60, pivot = origin) {
                    drawOval(a.copy(alpha = 0.16f + i * 0.08f), Offset(17f, 94f), Size(266f, 112f), style = Stroke(1.2f))
                    val theta = phase * 2 * PI.toFloat() + i * 2
                    val at = Offset(150f + cos(theta) * 133f, 150f + sin(theta) * 56f)
                    glow(at, 14f, a, 0.3f)
                    drawCircle(a, 3.5f, at)
                }
            }
            drawCircle(Brush.radialGradient(listOf(Color(0xFF201A3B), Color(0xFF111323)), origin, 80f), 80f, origin)
            ring(80f, a.copy(alpha = 0.25f), 1f)
        }
        "GRADIENT_GLOW" -> {
            repeat(7) { i ->
                val path = Path().apply {
                    moveTo(18f + i * 18f, 255f)
                    cubicTo(80f + i * 15f, 200f, 160f - i * 9f, 85f, 245f + i * 5f, 42f)
                }
                drawPath(path, (if (i < 4) a else b).copy(alpha = 0.08f + i * 0.015f),
                    style = Stroke(22f + breath * 4f, cap = StrokeCap.Round))
                drawPath(path, (if (i < 4) a else b).copy(alpha = 0.25f), style = Stroke(1f))
            }
            glow(origin, 91f, Color(0xFF0A1A26), 0.96f)
            stars(a, phase, 16)
        }
        "SOLAR_FLARE" -> {
            glow(origin, 147f, a, 0.14f)
            repeat(64) { i ->
                val angle = i * 360f / 64f
                val length = 8f + 8f * sin(i * 1.7f + phase * 2 * PI.toFloat())
                drawLine(a.copy(alpha = if (i % 4 == 0) 0.65f else 0.25f),
                    point(119f, angle), point(119 + length, angle), if (i % 4 == 0) 1.8f else 0.8f, StrokeCap.Round)
            }
            ring(104f, b.copy(alpha = 0.2f), 12f)
            ring(104f, a, 2.5f, -90f, level * 360)
            ring(96f, a.copy(alpha = 0.25f))
            targetDot(target, 104f, -90f, 360f)
        }
        "LUNAR_ECLIPSE" -> {
            stars(a, phase, 27)
            glow(Offset(136f, 148f), 127f, a, 0.12f)
            ring(113f, a.copy(alpha = 0.12f))
            // Draw crescent into a layer so its cutout keeps the underlying sky intact.
            val crescent = Path.combine(PathOperation.Difference,
                Path().apply { addOval(androidx.compose.ui.geometry.Rect(37f, 37f, 263f, 263f)) },
                Path().apply { addOval(androidx.compose.ui.geometry.Rect(58f, 20f, 280f, 242f)) })
            drawPath(crescent, diagonal(listOf(a.copy(alpha = 0.9f), b.copy(alpha = 0.22f))))
            drawCircle(a, 3f, point(130f, -35f + phase * 360f))
        }
        "OCEAN_DEPTH" -> {
            glow(Offset(150f, 235f), 130f, a, 0.09f)
            repeat(5) { i ->
                val y = 158f + i * 22f
                val wave = Path().apply {
                    moveTo(17f, 272f); lineTo(17f, y)
                    for (x in 17..283 step 3) lineTo(x.toFloat(), y + sin(x / 57f + i * 0.6f + phase * 2 * PI.toFloat()) * 10f)
                    lineTo(283f, 272f); close()
                }
                drawPath(wave, (if (i % 2 == 0) a else b).copy(alpha = 0.07f + i * 0.03f))
            }
            drawLine(a.copy(alpha = 0.2f), Offset(35f, 142f), Offset(265f, 142f), 1f)
            drawCircle(a, 2.5f, Offset(35f + 230f * level, 142f))
        }
        "ROSE_SILK" -> {
            glow(Offset(235f, 210f), 120f, b, 0.16f)
            repeat(9) { i ->
                val y = 158f + i * 9f
                val ribbon = Path().apply {
                    moveTo(12f, y + 50f)
                    cubicTo(80f, y - 55f + breath * 10, 195f, y + 91f, 286f, y - 5f)
                }
                drawPath(ribbon, diagonal(listOf(a.copy(alpha = 0.08f), a.copy(alpha = 0.5f), b.copy(alpha = 0.12f))),
                    style = Stroke(5f + i * 0.3f, cap = StrokeCap.Round))
            }
        }
        "GOLDEN_DIAL" -> {
            ring(129f, a.copy(alpha = 0.2f))
            ring(123f, a.copy(alpha = 0.1f), 4f)
            repeat(60) { i ->
                val d = i * 6f - 90f
                val major = i % 5 == 0
                drawLine(a.copy(alpha = if (major) 0.8f else 0.24f),
                    point(if (major) 106f else 112f, d), point(118f, d), if (major) 1.8f else 0.8f)
            }
            ring(94f, a.copy(alpha = 0.14f), 2f, 135f, 270f)
            ring(94f, a, 2f, 135f, 270 * level)
            targetDot(target, 94f)
            drawCircle(a.copy(alpha = 0.8f), 2.5f, Offset(150f, 222f))
            drawLine(a.copy(alpha = 0.4f), Offset(132f, 222f), Offset(142f, 222f), 1f)
            drawLine(a.copy(alpha = 0.4f), Offset(158f, 222f), Offset(168f, 222f), 1f)
        }
        "MONO_STUDIO" -> {
            drawLine(a.copy(alpha = 0.2f), Offset(32f, 48f), Offset(268f, 48f), 1f)
            drawLine(a.copy(alpha = 0.2f), Offset(32f, 246f), Offset(268f, 246f), 1f)
            repeat(18) { i ->
                drawRoundRect(a.copy(alpha = if (i < ceil(level * 18).toInt()) 0.9f else 0.08f),
                    Offset(32f + i * 13.2f, 215f), Size(9f, 9f + i * 0.5f), CornerRadius(1f))
            }
            drawLine(a, Offset(248f, 69f), Offset(268f, 69f), 1.5f)
            drawLine(a, Offset(268f, 69f), Offset(268f, 89f), 1.5f)
            drawCircle(a.copy(alpha = 0.4f), 3f, Offset(35f, 69f))
        }
        "CRYSTAL_FACETS" -> {
            glow(origin, 140f, a, 0.12f)
            val vertices = (0 until 6).map { point(if (it % 2 == 0) 132f else 116f, -90f + it * 60) }
            vertices.forEachIndexed { i, p ->
                val next = vertices[(i + 1) % 6]
                val facet = Path().apply { moveTo(150f, 150f); lineTo(p.x, p.y); lineTo(next.x, next.y); close() }
                drawPath(facet, diagonal(listOf((if (i % 2 == 0) a else b).copy(alpha = 0.08f + i * 0.025f), Color.Transparent), p, origin))
                drawLine(a.copy(alpha = 0.25f), p, next, 1f)
                drawLine(a.copy(alpha = 0.12f), p, origin, 0.8f)
            }
            glow(origin, 78f, Color(0xFF11182C), 0.98f)
            drawCircle(a.copy(alpha = 0.7f), 2.5f, vertices[0])
        }
        "BLOOM" -> {
            glow(origin, 144f, b, 0.1f)
            repeat(12) { i ->
                rotate(i * 30f + breath * 4, pivot = origin) {
                    drawOval(vertical(listOf(a.copy(alpha = 0.05f), b.copy(alpha = 0.4f))),
                        Offset(121f, 17f), Size(58f, 144f))
                    drawOval(a.copy(alpha = 0.2f), Offset(121f, 17f), Size(58f, 144f), style = Stroke(0.8f))
                }
            }
            drawCircle(Color(0xFF2B1A28), 76f, origin)
            ring(76f, a.copy(alpha = 0.4f))
        }
        "TOPOGRAPHIC" -> {
            repeat(10) { contour ->
                val r = 70f + contour * 7.4f
                val path = Path().apply {
                    for (i in 0..100) {
                        val angle = i * 2 * PI.toFloat() / 100
                        val radius = r + sin(angle * 3 + contour * 0.18f + phase * 2 * PI.toFloat()) * 6 +
                            cos(angle * 5 - contour * 0.2f) * 3
                        val x = 150 + cos(angle) * radius
                        val y = 150 + sin(angle) * radius * 0.86f
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, a.copy(alpha = 0.12f + contour * 0.035f), style = Stroke(if (contour % 3 == 0) 1.2f else 0.7f))
            }
        }
        "COMET_TRAIL" -> {
            stars(a, phase, 34)
            rotate(-25f, pivot = origin) {
                repeat(38) { i ->
                    val theta = phase * 2 * PI.toFloat() - i * 0.025f
                    val at = Offset(150 + cos(theta) * 132f, 150 + sin(theta) * 98f)
                    drawCircle(a.copy(alpha = (1f - i / 38f) * 0.8f), (3.3f - i * 0.07f).coerceAtLeast(0.5f), at)
                }
                val head = Offset(150 + cos(phase * 2 * PI.toFloat()) * 132f, 150 + sin(phase * 2 * PI.toFloat()) * 98f)
                glow(head, 22f, b, 0.6f)
                drawCircle(Color.White, 3.5f, head)
            }
            glow(origin, 91f, Color(0xFF11172D), 0.98f)
            ring(91f, a.copy(alpha = 0.12f))
        }
        "SUNSET_HORIZON" -> {
            glow(Offset(150f, 118f), 145f, a, 0.1f)
            drawCircle(vertical(listOf(a.copy(alpha = 0.55f), b.copy(alpha = 0.16f))), 92f, Offset(150f, 135f))
            repeat(9) { i ->
                val y = 142f + i * 9f
                drawLine(t.backgroundColor.copy(alpha = 0.6f), Offset(58f, y), Offset(242f, y), 2f + i * 0.6f)
            }
            val hills = Path().apply {
                moveTo(18f, 225f); cubicTo(70f, 199f, 90f, 214f, 132f, 205f)
                cubicTo(188f, 179f, 214f, 206f, 282f, 193f); lineTo(282f, 274f); lineTo(18f, 274f); close()
            }
            drawPath(hills, b.copy(alpha = 0.24f))
            translate(top = 20f) { drawPath(hills, Color(0xFF281C32)) }
            glow(origin, 75f, Color(0xFF211623), 0.85f)
        }
    }
    if (reached) {
        // Completion is a small accent; retain the selected design's identity.
        drawCircle(Color(0xFFD8F4C7), 3f, Offset(150f, 286f))
    }
}

private fun DrawScope.stars(color: Color, phase: Float, count: Int) {
    repeat(count) { i ->
        val x = 18f + ((i * 73 + 17) % 264)
        val y = 18f + ((i * 47 + 29) % 264)
        if (hypot(x - 150, y - 150) > 84) {
            val alpha = 0.16f + 0.3f * (0.5f + 0.5f * sin(phase * 2 * PI.toFloat() + i))
            drawCircle(color.copy(alpha = alpha), if (i % 5 == 0) 1.6f else 0.9f, Offset(x, y))
        }
    }
}


private fun vertical(colors: List<Color>) = Brush.verticalGradient(colors, startY = 0f, endY = 300f)
private fun diagonal(colors: List<Color>, start: Offset = Offset.Zero, end: Offset = Offset(300f, 300f)) =
    Brush.linearGradient(colors, start, end)
