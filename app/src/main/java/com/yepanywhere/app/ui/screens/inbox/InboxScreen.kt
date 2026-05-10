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
import androidx.compose.runtime.collectAsState
import com.yepanywhere.app.data.model.AgentActivity
import com.yepanywhere.app.data.model.InboxItem
import com.yepanywhere.app.data.model.Project
import com.yepanywhere.app.data.remote.ApiService
import com.yepanywhere.app.ui.components.SessionCard
import com.yepanywhere.app.ui.theme.*

enum class InboxTab(val label: String) { INBOX("收件箱"), ALL("全部") }
enum class InboxFilter(val label: String) { ALL("全部"), ACTIVE("活跃"), ATTENTION("需关注") }

@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    api: ApiService,
    onSessionClick: (projectId: String, sessionId: String, sessionTitle: String) -> Unit
) {
    val items by viewModel.items.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTab by remember { mutableStateOf(InboxTab.INBOX) }
    var selectedFilter by remember { mutableStateOf(InboxFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var longPressedItem by remember { mutableStateOf<InboxItem?>(null) }
    var showNewSessionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInbox(api)
        viewModel.loadAllSessions(api)
    }

    val displayItems = if (selectedTab == InboxTab.INBOX) items else allSessions

    val filteredItems = displayItems.filter { item ->
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
            IconButton(onClick = { showNewSessionDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "新建", tint = Tint, modifier = Modifier.size(28.dp))
            }
        }

        // Tab selector: 收件箱 / 全部
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            InboxTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Tint else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tab.label,
                        style = YepType.subheadline,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
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

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            InboxFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { selectedFilter = filter }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        filter.label,
                        style = YepType.caption1,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Content
        when {
            isLoading && displayItems.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Tint)
                }
            }
            error != null && displayItems.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("加载失败", style = YepType.headline, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(4.dp))
                        Text(error ?: "", style = YepType.subheadline, color = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.loadInbox(api)
                                viewModel.loadAllSessions(api)
                            },
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
                    items(filteredItems, key = { "${it.projectId}_${it.sessionId}" }) { item ->
                        Box {
                            SessionCard(
                                item = item,
                                onClick = { onSessionClick(item.projectId, item.sessionId, item.sessionTitle) },
                                onLongClick = { longPressedItem = item }
                            )
                            DropdownMenu(
                                expanded = longPressedItem?.sessionId == item.sessionId,
                                onDismissRequest = { longPressedItem = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("打开") },
                                    onClick = {
                                        longPressedItem = null
                                        onSessionClick(item.projectId, item.sessionId, item.sessionTitle)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("复制会话ID") },
                                    onClick = { longPressedItem = null }
                                )
                                DropdownMenuItem(
                                    text = { Text("标记已读") },
                                    onClick = { longPressedItem = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New session dialog
    if (showNewSessionDialog) {
        NewSessionDialog(
            projects = projects,
            onDismiss = { showNewSessionDialog = false },
            onCreate = { project, title ->
                showNewSessionDialog = false
                viewModel.createSession(api, project.id, title) { projectId, sessionId, sessionTitle ->
                    onSessionClick(projectId, sessionId, sessionTitle)
                }
            }
        )
    }
}

@Composable
private fun NewSessionDialog(
    projects: List<Project>,
    onDismiss: () -> Unit,
    onCreate: (Project, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedProject by remember(projects) { mutableStateOf(projects.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建会话") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("会话标题") },
                    placeholder = { Text("输入会话标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                if (projects.isNotEmpty()) {
                    Text("选择项目", style = YepType.subheadline, color = MaterialTheme.colorScheme.outline)
                    projects.forEach { project ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedProject?.id == project.id)
                                        Tint.copy(alpha = 0.1f)
                                    else Color.Transparent
                                )
                                .clickable { selectedProject = project }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedProject?.id == project.id,
                                onClick = { selectedProject = project },
                                colors = RadioButtonDefaults.colors(selectedColor = Tint)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(project.name, style = YepType.body)
                        }
                    }
                } else {
                    Text("加载项目中...", style = YepType.subheadline, color = MaterialTheme.colorScheme.outline)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val project = selectedProject
                    if (project != null && title.isNotBlank()) {
                        onCreate(project, title.trim())
                    }
                },
                enabled = selectedProject != null && title.isNotBlank()
            ) {
                Text("创建", color = if (selectedProject != null && title.isNotBlank()) Tint else MaterialTheme.colorScheme.outline)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
