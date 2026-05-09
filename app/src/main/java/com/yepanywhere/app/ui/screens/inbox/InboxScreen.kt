package com.yepanywhere.app.ui.screens.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yepanywhere.app.data.model.AgentActivity
import com.yepanywhere.app.data.model.InboxItem
import com.yepanywhere.app.data.remote.ApiService
import com.yepanywhere.app.ui.components.SessionCard
import com.yepanywhere.app.ui.theme.*

enum class InboxFilter(val label: String) { ALL("全部"), ACTIVE("活跃"), ATTENTION("需关注") }

@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    api: ApiService,
    onSessionClick: (projectId: String, sessionId: String, sessionTitle: String) -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(InboxFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadInbox(api) }

    val filteredItems = items.filter { item ->
        val matchesFilter = when (selectedFilter) {
            InboxFilter.ALL -> true
            InboxFilter.ACTIVE -> item.activity == AgentActivity.IN_TURN
            InboxFilter.ATTENTION -> item.pendingInputType != null
        }
        val matchesSearch = searchQuery.isBlank() ||
            item.sessionTitle.contains(searchQuery, ignoreCase = true) ||
            item.projectName.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("会话", style = YepType.largeTitle, color = MaterialTheme.colorScheme.onBackground)
            IconButton(onClick = { /* TODO: new session */ }) {
                Icon(Icons.Default.Add, contentDescription = "新建", tint = Tint, modifier = Modifier.size(28.dp))
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("搜索会话", style = YepType.body, color = MaterialTheme.colorScheme.outline) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Segmented control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            InboxFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Tint else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedFilter = filter }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        filter.label,
                        style = YepType.subheadline,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Content
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Tint)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("加载失败", style = YepType.headline, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(4.dp))
                        Text(error ?: "", style = YepType.subheadline, color = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadInbox(api) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Tint)
                        ) { Text("重试") }
                    }
                }
            }
            filteredItems.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无会话", style = YepType.headline, color = MaterialTheme.colorScheme.outline)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filteredItems, key = { it.sessionId }) { item ->
                        SessionCard(
                            item = item,
                            onClick = { onSessionClick(item.projectId, item.sessionId, item.sessionTitle) }
                        )
                    }
                }
            }
        }
    }
}
