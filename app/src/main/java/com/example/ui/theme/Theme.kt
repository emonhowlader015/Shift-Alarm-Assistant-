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
    primary = ElectricCyan,
    onPrimary = MidnightDark,
    primaryContainer = MidnightNavyElevated,
    onPrimaryContainer = ElectricCyan,
    secondary = VividPurple,
    onSecondary = Color.White,
    secondaryContainer = MidnightCard,
    onSecondaryContainer = VividPurple,
    tertiary = GlowingMagenta,
    onTertiary = Color.White,
    background = MidnightDark,
    onBackground = TextPrimaryDark,
    surface = MidnightNavy,
    onSurface = TextPrimaryDark,
    surfaceVariant = MidnightCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = MidnightCardBorder,
    error = CoralError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeonBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = NeonBlue,
    secondary = RoyalViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = RoyalViolet,
    tertiary = GlowingMagenta,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = CardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1),
    error = CoralError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to stunning dark theme for luminous water-drop effects
    dynamicColor: Boolean = false, // Keep branded RGB theme intact
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
