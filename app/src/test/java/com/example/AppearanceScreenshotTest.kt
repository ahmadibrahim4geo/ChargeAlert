package com.example

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.model.AlertSettings
import com.example.model.BatteryInfo
import com.example.ui.ChargingAppearanceCard
import com.example.ui.ChargingScreen
import com.example.ui.ChargingThemeTile
import com.example.ui.theme.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h851dp-xxhdpi", sdk = [34], application = Application::class)
class AppearanceScreenshotTest {
    @get:Rule val compose = createComposeRule()
    private val sample = BatteryInfo(level = 76, isCharging = true, temperature = 32.4f, voltage = 4180)

    @Test fun everyDesignKeepsItsIdentityInBatterySaver() {
        var selected by mutableStateOf(LOCKSCREEN_THEMES_LIST.first().id)
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                ChargingScreen(sample, AlertSettings(lockscreenThemeId = selected), powerSave = true,
                    isAppearancePreview = true, onClose = {})
            }
        }
        for (design in LOCKSCREEN_THEMES_LIST) {
            compose.runOnIdle { selected = design.id }
            compose.onNodeWithContentDescription("${design.nameAr}، البطارية 76%").assertIsDisplayed()
            compose.onNodeWithText("76%").assertIsDisplayed()
            compose.onRoot().captureRoboImage(filePath = "src/test/screenshots/appearance/${design.id.lowercase()}.png")
        }
    }

    @Test fun gallerySelectionUpdatesThePreviewAndCanOpenIt() {
        var selected by mutableStateOf("NEON_RING")
        var opened = false
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        ChargingAppearanceCard(false, AlertSettings(lockscreenThemeId = selected, animationsEnabled = false),
                            onEnabledChanged = {}, onThemeSelected = { selected = it }, onPreview = { opened = true })
                    }
                }
            }
        }
        compose.onNodeWithTag("theme_card_MINIMAL_GLASS").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("MINIMAL_GLASS", selected) }
        compose.onNodeWithTag("theme_card_MINIMAL_GLASS").assertIsSelected()
        compose.onNodeWithText("معاينة شاشة الشحن كاملة").performScrollTo().performClick()
        assertTrue(opened)
        compose.onRoot().captureRoboImage(filePath = "src/test/screenshots/appearance/gallery.png")
    }

    @Test @Config(qualifiers = "w1000dp-h1750dp-mdpi")
    fun twentyDesignContactSheet() {
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(Modifier.fillMaxSize().background(Color(0xFF080D15)).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("مجموعة مظهر الشحن · ٢٠ تصميم", color = Color.White, fontSize = 26.sp)
                        Text("١٠ تصميمات مطوّرة + ١٠ أنماط جديدة · معاينات ببيانات توضيحية",
                            color = Color(0xFF98A6B8), fontSize = 15.sp)
                        LOCKSCREEN_THEMES_LIST.chunked(4).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                row.forEach { design ->
                                    ChargingThemeTile(design, false, false, {}, Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage(filePath = "src/test/screenshots/appearance/all-20-designs.png")
    }

    @Test fun catalogPreservesSavedSelectionsAndAddsTenDesigns() {
        assertEquals(20, LOCKSCREEN_THEMES_LIST.map { it.id }.toSet().size)
        assertEquals(10, LOCKSCREEN_THEMES_LIST.count { it.isNew })
        listOf("NEON_RING", "MINIMAL_GLASS", "HOLOGRAM_WAVE", "ARC_REACTOR", "MATRIX_PULSE",
            "ULTRA_BAR", "DIGITAL_CLOCK", "ENERGY_SPHERE", "GRADIENT_GLOW", "SOLAR_FLARE").forEach {
            assertEquals(it, findChargingTheme(it).id)
        }
        assertEquals("NEON_RING", findChargingTheme("unknown-legacy-theme").id)
    }
}
