package com.yepanywhere.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Tint,
    onPrimary = Surface,
    primaryContainer = Green,
    secondary = Purple,
    background = Background,
    onBackground = Label,
    surface = Surface,
    onSurface = Label,
    surfaceVariant = Fill,
    outline = Label2,
    error = Red,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkTint,
    onPrimary = DarkSurface,
    primaryContainer = DarkGreen,
    secondary = DarkPurple,
    background = DarkBackground,
    onBackground = DarkLabel,
    surface = DarkSurface,
    onSurface = DarkLabel,
    surfaceVariant = DarkFill,
    outline = DarkLabel2,
    error = DarkRed,
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
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
