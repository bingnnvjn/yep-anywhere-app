package com.yepanywhere.app.ui.screens.files

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.yepanywhere.app.data.model.FileEntry
import com.yepanywhere.app.data.remote.ApiService
import com.yepanywhere.app.ui.components.FileRow
import com.yepanywhere.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel,
    api: ApiService,
    projectId: String,
    projectName: String,
    onBack: () -> Unit
) {
    val files by viewModel.files.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Breadcrumb path segments
    val pathSegments = remember(currentPath) {
        if (currentPath.isBlank()) emptyList()
        else currentPath.split("/").filter { it.isNotBlank() }
    }

    LaunchedEffect(projectId) {
        viewModel.loadFiles(api, projectId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top bar
        TopAppBar(
            title = {
                Column {
                    Text(projectName, style = YepType.headline, maxLines = 1)
                    Text("main", style = YepType.caption1, color = MaterialTheme.colorScheme.outline)
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (pathSegments.isNotEmpty()) {
                        val parentPath = pathSegments.dropLast(1).joinToString("/")
                        viewModel.loadFiles(api, projectId, parentPath)
                    } else {
                        onBack()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        // Git banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            color = Tint.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("main", style = YepType.subheadline, color = Tint, fontWeight = FontWeight.SemiBold)
                Text("•", color = MaterialTheme.colorScheme.outline)
                Text("${files.size} 项", style = YepType.subheadline, color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.weight(1f))
                Text("查看 diff", style = YepType.subheadline, color = Tint)
            }
        }

        // Breadcrumb
        if (pathSegments.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "根目录",
                    style = YepType.subheadline,
                    color = Tint,
                    modifier = Modifier.clickable { viewModel.loadFiles(api, projectId, "") }
                )
                pathSegments.forEachIndexed { index, segment ->
                    Text(" › ", style = YepType.subheadline, color = MaterialTheme.colorScheme.outline)
                    val isLast = index == pathSegments.lastIndex
                    Text(
                        segment,
                        style = YepType.subheadline,
                        color = if (isLast) MaterialTheme.colorScheme.onSurface else Tint,
                        modifier = if (!isLast) Modifier.clickable {
                            val path = pathSegments.take(index + 1).joinToString("/")
                            viewModel.loadFiles(api, projectId, path)
                        } else Modifier
                    )
                }
            }
        }

        // File list
        when {
            isLoading -> {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Tint)
                }
            }
            files.isEmpty() -> {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("空目录", style = YepType.headline, color = MaterialTheme.colorScheme.outline)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            files.forEach { entry ->
                                FileRow(
                                    entry = entry,
                                    onClick = {
                                        if (entry.isDirectory) {
                                            viewModel.loadFiles(api, projectId, entry.path)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
