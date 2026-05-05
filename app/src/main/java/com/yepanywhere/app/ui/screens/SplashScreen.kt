package com.yepanywhere.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.6f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF22c55e)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Official Yep Anywhere icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Recreate the checkmark inside the white box
                androidx.compose.foundation.Canvas(modifier = Modifier.size(64.dp)) {
                    val strokeWidth = 8f
                    // Green checkmark
                    drawLine(
                        color = Color(0xFF22c55e),
                        start = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.45f),
                        end = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.65f),
                        strokeWidth = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF22c55e),
                        start = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.65f),
                        end = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.25f),
                        strokeWidth = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    // Downward line
                    drawLine(
                        color = Color(0xFF22c55e),
                        start = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.65f),
                        end = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.85f),
                        strokeWidth = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Yep Anywhere",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "随时随地，AI 编码助手",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
