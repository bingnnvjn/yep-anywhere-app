package com.yepanywhere.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yepanywhere.app.data.model.AgentActivity
import com.yepanywhere.app.data.model.InboxItem
import com.yepanywhere.app.ui.theme.*

@Composable
fun SessionCard(
    item: InboxItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "press")

    val needsAttention = item.pendingInputType != null
    val isRunning = item.activity == AgentActivity.IN_TURN

    val avatarColor = when {
        needsAttention -> Brush.linearGradient(listOf(Red, Color(0xFFFF6B6B)))
        isRunning -> Brush.linearGradient(listOf(Green, Color(0xFF30D158)))
        else -> Brush.linearGradient(listOf(Color(0xFF8E8E93), Color(0xFFAEAEB2)))
    }

    Row(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.sessionTitle.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontSize = YepType.headline.fontSize,
                fontWeight = FontWeight.SemiBold
            )
            // Online indicator
            if (isRunning) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Green)
                )
            }
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.sessionTitle,
                    style = YepType.headline,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.updatedAt.takeLast(5),
                    style = YepType.subheadline,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val previewColor = if (needsAttention) Red else MaterialTheme.colorScheme.outline
                Text(
                    text = if (needsAttention) "需要审批" else item.projectName,
                    style = YepType.subheadline,
                    color = previewColor,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (needsAttention) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(11.dp))
                            .background(Red)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "1",
                            color = Color.White,
                            style = YepType.footnote,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
