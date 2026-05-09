package com.yepanywhere.app.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.yepanywhere.app.ui.theme.GlassDark
import com.yepanywhere.app.ui.theme.GlassLight

@Composable
fun FrostedGlassBar(
    modifier: Modifier = Modifier,
    blurRadius: Float = 20f,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) GlassDark else GlassLight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    renderEffect = android.graphics.RenderEffect
                        .createBlurEffect(blurRadius, blurRadius, android.graphics.Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                }
            }
            .background(bgColor),
        content = content
    )
}
