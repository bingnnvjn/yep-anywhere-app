package com.yepanywhere.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GradientStart,
    onPrimary = TextOnGradient,
    primaryContainer = GradientStartLight,
    secondary = GradientEnd,
    onSecondary = TextOnGradient,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = CardLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    outline = TextSecondary,
)

private val DarkColorScheme = darkColorScheme(
    primary = GradientStartLight,
    onPrimary = TextPrimary,
    primaryContainer = GradientStart,
    secondary = GradientEndLight,
    onSecondary = TextOnGradient,
    background = SurfaceDark,
    onBackground = TextOnGradient,
    surface = CardDark,
    onSurface = TextOnGradient,
    surfaceVariant = SurfaceDark,
    outline = TextSecondary,
)

@Composable
fun YepAnywhereTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
