package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import com.example.model.AlertSettings
import com.example.model.BatteryInfo
import com.example.ui.ChargingScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h851dp-xxhdpi", sdk = [34], application = Application::class)
class GreetingScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()
    private val battery = BatteryInfo(level = 76, isCharging = true, temperature = 32.4f, voltage = 4180)

    @Test fun arabicChargingScreen() {
        var closed = false
        composeTestRule.setContent {
            MyApplicationTheme {
                ChargingScreen(battery, AlertSettings(animationsEnabled = false), onClose = { closed = true })
            }
        }
        composeTestRule.onNodeWithText("76%").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/charging-ar.png")
        composeTestRule.onNodeWithText("إغلاق شاشة الشحن").performScrollTo().performClick()
        assertTrue(closed)
    }

    @Test fun previewShowsRealReadingWithoutClaimingTargetReached() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ChargingScreen(battery, AlertSettings(language = "en", animationsEnabled = false),
                    isTest = true, onClose = {})
            }
        }
        composeTestRule.onNodeWithText("76%").assertIsDisplayed()
        composeTestRule.onNodeWithText("ALARM PREVIEW").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/charging-en-preview.png")
    }

    @Test fun animatedThemeUsesTheRealBatteryReading() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ChargingScreen(battery, AlertSettings(language = "en"), onClose = {})
            }
        }
        composeTestRule.onNodeWithText("76%").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/charging-neon.png")
    }

    @Test @Config(qualifiers = "w320dp-h640dp-xhdpi")
    fun smallArabicScreenSupportsScrollingAndLargeText() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val density = androidx.compose.ui.platform.LocalDensity.current
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density, 1.3f)
                ) {
                    ChargingScreen(battery, AlertSettings(animationsEnabled = false), onClose = {})
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/charging-compact-top.png")
        composeTestRule.onNodeWithTag("charging_content").performScrollToNode(hasText("إغلاق شاشة الشحن"))
        composeTestRule.onNodeWithText("إغلاق شاشة الشحن").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/charging-compact.png")
    }
}
