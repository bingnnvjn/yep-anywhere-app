package com.yepanywhere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yepanywhere.app.ui.theme.*

@Composable
fun ApprovalCard(
    title: String,
    body: String,
    detail: String?,
    onDeny: () -> Unit,
    onAllow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Red.copy(alpha = 0.05f))
            .border(0.5.dp, Red.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Text("需要审批", style = YepType.caption1, color = Red, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(body, style = YepType.body, color = MaterialTheme.colorScheme.onSurface)
        if (detail != null) {
            Spacer(Modifier.height(2.dp))
            Text(detail, style = YepType.subheadline, color = MaterialTheme.colorScheme.outline)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Deny button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onDeny() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("拒绝", style = YepType.body, color = Red)
            }
            // Allow button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Tint)
                    .clickable { onAllow() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("允许", style = YepType.body, color = Color.White)
            }
        }
    }
}
