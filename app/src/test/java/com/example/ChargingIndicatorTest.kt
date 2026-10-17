package com.example

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import com.example.model.AlertSettings
import com.example.model.BatteryInfo
import com.example.ui.ChargingScreen
import com.example.ui.theme.MyApplicationTheme
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
class ChargingIndicatorTest {
    @get:Rule val compose = createComposeRule()
    private val reading = BatteryInfo(level = 76, isCharging = true, temperature = 32.4f, voltage = 4180)

    @Test fun animationPulsesWithoutChangingTheActualPercentage() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MyApplicationTheme(darkTheme = true) { ChargingScreen(reading, AlertSettings(), onClose = {}) }
        }
        compose.mainClock.advanceTimeByFrame()
        val gauge = compose.onNodeWithContentDescription("هالة النيون، البطارية 76%")
        gauge.assertIsDisplayed()
        gauge.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "الشحن الحالي 76%. الهدف 80%."))
        compose.onRoot().captureRoboImage(filePath = "src/test/screenshots/indicator/pulse-start.png")
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithText("76%").assertIsDisplayed()
        assertEquals(0.76f, gauge.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current)
        compose.onRoot().captureRoboImage(filePath = "src/test/screenshots/indicator/pulse-after.png")
    }

    @Test fun movementHasAClearReasonWhenStopped() {
        var saver by mutableStateOf(true)
        var settings by mutableStateOf(AlertSettings())
        var battery by mutableStateOf(reading)
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                ChargingScreen(battery, settings, powerSave = saver, onClose = {})
            }
        }
        compose.onNodeWithText("الحركة متوقفة لتوفير الطاقة").assertIsDisplayed()
        compose.runOnIdle { saver = false; settings = settings.copy(animationsEnabled = false) }
        compose.onNodeWithText("الحركة مقفولة من إعداداتك").assertIsDisplayed()
        compose.runOnIdle { settings = settings.copy(animationsEnabled = true); battery = reading.copy(isCharging = false, isPowerConnected = true) }
        compose.onNodeWithText("النبضة تبدأ عندما يكون الشحن شغّالًا").assertIsDisplayed()
        compose.onNodeWithText("76%").assertIsDisplayed()
    }

    @Test fun unknownReadingIsNotShownAsZeroPercent() {
        compose.setContent {
            MyApplicationTheme(darkTheme = true) { ChargingScreen(BatteryInfo(), AlertSettings(), onClose = {}) }
        }
        val gauge = compose.onNodeWithContentDescription("هالة النيون، البطارية —")
        assertEquals(androidx.compose.ui.semantics.ProgressBarRangeInfo.Indeterminate,
            gauge.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo])
        compose.onNodeWithText("0%").assertDoesNotExist()
    }

    @Test fun adaptiveAndMonochromeIconsRender() {
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                Column(Modifier.fillMaxSize().background(Color(0xFF101B2B)).padding(24.dp)) {
                    Text("Charge Alert", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        listOf(R.mipmap.ic_launcher, R.drawable.ic_launcher_monochrome).forEach { resource ->
                            AndroidView(factory = { context ->
                                ImageView(context).apply { setImageResource(resource) }
                            }, modifier = Modifier.size(140.dp))
                        }
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage(filePath = "src/test/screenshots/indicator/app-icons.png")
    }
}
