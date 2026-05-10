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

/**
 * Extract displayable text from message content.
 * API returns content as one of:
 * - String
 * - List of content blocks: [{"type":"text","text":"hello"}, ...]
 * - Single content block: {"type":"text","text":"hello"}
 * - null
 */
fun extractMessageText(content: Any?): String {
    return when (val c = content) {
        is String -> c
        is List<*> -> c.mapNotNull { block ->
            when (block) {
                is Map<*, *> -> {
                    val type = block["type"] as? String
                    when (type) {
                        "text" -> block["text"] as? String
                        "tool_use" -> {
                            val name = block["name"] as? String ?: "tool"
                            "[调用工具: $name]"
                        }
                        "tool_result" -> {
                            // tool_result content can be string or list of blocks
                            val inner = block["content"]
                            extractMessageText(inner)
                        }
                        else -> block["text"] as? String
                    }
                }
                is String -> block
                else -> null
            }
        }.joinToString("\n")
        is Map<*, *> -> {
            val type = c["type"] as? String
            when (type) {
                "text" -> c["text"] as? String ?: ""
                "tool_use" -> {
                    val name = c["name"] as? String ?: "tool"
                    "[调用工具: $name]"
                }
                "tool_result" -> extractMessageText(c["content"])
                else -> c["text"] as? String ?: c.toString()
            }
        }
        null -> ""
        else -> c.toString()
    }.trim()
}

@Composable
fun MessageBubble(message: Message, modifier: Modifier = Modifier) {
    val isOutgoing = message.role == MessageRole.USER
    val text = extractMessageText(message.content)

    // Don't render empty bubbles
    if (text.isBlank() && !message.isStreaming) return

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

        Text(
            text = text.ifBlank { "…" },
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
