package com.yepanywhere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yepanywhere.app.ui.theme.Green
import com.yepanywhere.app.ui.theme.Red

data class DiffLine(
    val lineNum: Int,
    val content: String,
    val type: DiffLineType
)

enum class DiffLineType { ADD, DELETE, CONTEXT }

@Composable
fun DiffViewer(lines: List<DiffLine>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1C1C1E))
    ) {
        lines.forEach { line ->
            val bg = when (line.type) {
                DiffLineType.ADD -> Green.copy(alpha = 0.1f)
                DiffLineType.DELETE -> Red.copy(alpha = 0.06f)
                DiffLineType.CONTEXT -> Color.Transparent
            }
            val textColor = when (line.type) {
                DiffLineType.ADD -> Green
                DiffLineType.DELETE -> Red
                DiffLineType.CONTEXT -> Color(0xFFAAAAAA)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(horizontal = 10.dp, vertical = 1.dp)
            ) {
                Text(
                    "${line.lineNum}",
                    modifier = Modifier.width(24.dp),
                    color = Color(0x40FFFFFF),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    line.content,
                    color = textColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
