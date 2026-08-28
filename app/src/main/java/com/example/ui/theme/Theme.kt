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
    primary = NeonHotPink,
    onPrimary = Color.White,
    primaryContainer = NeonDeepViolet,
    onPrimaryContainer = NeonLaserCyan,
    secondary = NeonLaserCyan,
    onSecondary = Color(0xFF080214),
    secondaryContainer = Color(0xFF1F093D),
    onSecondaryContainer = NeonLaserCyan,
    tertiary = NeonYellowGlow,
    background = JoeyDarkBackground,
    onBackground = JoeyDarkTextPrimary,
    surface = JoeyDarkSurface,
    onSurface = JoeyDarkTextPrimary,
    surfaceVariant = JoeyDarkSurfaceVariant,
    onSurfaceVariant = JoeyDarkTextSecondary,
    outline = JoeyDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = NeonMagentaPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = NeonDeepViolet,
    secondary = NeonLaserCyan,
    onSecondary = Color(0xFF080214),
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    tertiary = NeonYellowGlow,
    background = JoeyLightBackground,
    onBackground = JoeyLightTextPrimary,
    surface = JoeyLightSurface,
    onSurface = JoeyLightTextPrimary,
    surfaceVariant = JoeyLightSurfaceVariant,
    onSurfaceVariant = JoeyLightTextSecondary,
    outline = JoeyLightCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use Joey's signature cyberpunk palette
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
