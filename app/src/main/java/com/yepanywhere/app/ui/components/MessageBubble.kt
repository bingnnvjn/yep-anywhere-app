package com.yepanywhere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yepanywhere.app.data.model.Message
import com.yepanywhere.app.data.model.MessageRole
import com.yepanywhere.app.ui.theme.*

@Composable
fun MessageBubble(message: Message, modifier: Modifier = Modifier) {
    val isOutgoing = message.role == MessageRole.USER
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        val shape = if (isOutgoing)
            RoundedCornerShape(20.dp, 20.dp, 5.dp, 20.dp)
        else
            RoundedCornerShape(20.dp, 20.dp, 20.dp, 5.dp)

        val bgColor = if (isOutgoing) BubbleOutgoingLight else BubbleIncomingLight
        val textColor = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface

        val text = when (val c = message.content) {
            is String -> c
            else -> c.toString()
        }

        Text(
            text = text,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bgColor)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            color = textColor,
            style = YepType.body
        )
    }
}
