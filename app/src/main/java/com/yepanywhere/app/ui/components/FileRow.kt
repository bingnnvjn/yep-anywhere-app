package com.yepanywhere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yepanywhere.app.data.model.FileEntry
import com.yepanywhere.app.data.model.FileStatus
import com.yepanywhere.app.ui.theme.*

@Composable
fun FileRow(
    entry: FileEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nameColor = when (entry.status) {
        FileStatus.NEW -> Green
        FileStatus.DELETED -> Red
        else -> MaterialTheme.colorScheme.onSurface
    }
    val textDecoration = if (entry.status == FileStatus.DELETED) TextDecoration.LineThrough else null

    val badgeColor = when (entry.status) {
        FileStatus.NEW -> Green
        FileStatus.MODIFIED -> Orange
        FileStatus.DELETED -> Red
        else -> null
    }
    val badgeText = when (entry.status) {
        FileStatus.NEW -> "N"
        FileStatus.MODIFIED -> "M"
        FileStatus.DELETED -> "D"
        else -> null
    }
    val bgColor = when (entry.status) {
        FileStatus.NEW -> Green.copy(alpha = 0.04f)
        FileStatus.MODIFIED -> Orange.copy(alpha = 0.04f)
        FileStatus.DELETED -> Red.copy(alpha = 0.04f)
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // File/folder icon
        Text(
            if (entry.isDirectory) "📁" else "📄",
            fontSize = 20.sp,
            modifier = Modifier.width(28.dp)
        )

        // File name
        Text(
            text = entry.name,
            style = YepType.body,
            color = nameColor,
            textDecoration = textDecoration,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Status badge
        if (badgeText != null && badgeColor != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(badgeColor)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(badgeText, color = Color.White, fontSize = 11.sp)
            }
        }

        // Chevron
        Text(
            "›",
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            fontSize = 14.sp
        )
    }
}
