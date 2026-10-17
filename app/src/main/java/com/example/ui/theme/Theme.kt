package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color(0xFF00381B),
    primaryContainer = Color(0xFF00522B),
    onPrimaryContainer = MintGreen,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF004D63),
    onSecondaryContainer = DeepCyan,
    tertiary = AccentPurple,
    background = DarkBackground,
    onBackground = Color(0xFFE2E9F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E9F0),
    surfaceVariant = DarkCardVariant,
    onSurfaceVariant = Color(0xFFA5B7C7),
    outline = Color(0xFF2C3E50),
    outlineVariant = Color(0xFF1E2D3D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF009C4D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB4F6CD),
    onPrimaryContainer = Color(0xFF00210E),
    secondary = Color(0xFF007A9E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCBEBFF),
    onSecondaryContainer = Color(0xFF001E2A),
    background = LightBackground,
    onBackground = Color(0xFF161C22),
    surface = LightSurface,
    onSurface = Color(0xFF161C22),
    surfaceVariant = LightCardVariant,
    onSurfaceVariant = Color(0xFF455563),
    outline = Color(0xFFC2D1DE),
    outlineVariant = Color(0xFFE1EBF2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
