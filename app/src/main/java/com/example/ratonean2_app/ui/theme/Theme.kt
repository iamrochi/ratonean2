package com.example.ratonean2_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RatoneanPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = RatoneanPrimaryContainerDark,
    onPrimaryContainer = Color.White,
    background = RatoneanBackgroundDark,
    surface = RatoneanSurfaceDark,
    onBackground = RatoneanOnBackgroundDark,
    onSurface = RatoneanOnSurfaceDark,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = RatoneanError
)

private val LightColorScheme = lightColorScheme(
    primary = RatoneanPrimary,
    onPrimary = RatoneanOnPrimary,
    primaryContainer = RatoneanPrimaryContainer,
    background = RatoneanBackgroundLight,
    surface = RatoneanSurfaceLight,
    onBackground = RatoneanOnBackgroundLight,
    onSurface = RatoneanOnSurfaceLight,
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF6B7280),
    error = RatoneanError
)

@Composable
fun Ratonean2appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
