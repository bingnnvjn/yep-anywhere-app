package com.yepanywhere.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yepanywhere.app.data.model.PendingInput
import com.yepanywhere.app.data.model.Message
import com.yepanywhere.app.data.remote.ApiService
import com.yepanywhere.app.ui.components.MessageBubble
import com.yepanywhere.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    api: ApiService,
    projectId: String,
    sessionId: String,
    sessionTitle: String,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val agentStatus by viewModel.agentStatus.collectAsState()
    val pendingInput by viewModel.pendingInput.collectAsState()
    val permissionMode by viewModel.permissionMode.collectAsState()
    val showModeMenu by viewModel.showModeMenu.collectAsState()
    val sheetState = rememberModalBottomSheetState(confirmValueChange = { false })
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(projectId, sessionId) {
        viewModel.loadSession(api, projectId, sessionId)
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        sessionTitle,
                        style = YepType.headline,
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Green)
                        )
                        Text("Claude Code", style = YepType.caption1, color = MaterialTheme.colorScheme.outline)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("刷新") },
                            onClick = {
                                showMenu = false
                                viewModel.loadSession(api, projectId, sessionId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("复制会话ID") },
                            onClick = {
                                showMenu = false
                                // TODO: clipboard copy
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("查看项目文件") },
                            onClick = {
                                showMenu = false
                                // TODO: navigate to files
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        // Context bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("上下文", style = YepType.caption1, color = MaterialTheme.colorScheme.outline)
                LinearProgressIndicator(
                    progress = { 0.45f },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Tint,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                )
                Text("45%", style = YepType.caption1, color = MaterialTheme.colorScheme.outline)
            }
        }

        // Messages
        when {
            isLoading -> {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Tint)
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }
                    if (agentStatus != AgentStatus.IDLE) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                val statusText = when (agentStatus) {
                                    AgentStatus.THINKING -> "Claude 正在思考..."
                                    AgentStatus.WAITING_INPUT -> "Claude 等待输入"
                                    AgentStatus.IDLE -> ""
                                }
                                Text(
                                    statusText,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 5.dp))
                                        .background(BubbleIncomingLight)
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outline,
                                    style = YepType.body
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Permission mode button
                Box {
                    IconButton(
                        onClick = { viewModel.toggleModeMenu() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(
                            when (permissionMode) {
                                "bypassPermissions" -> "🛡️"
                                "acceptEdits" -> "✏️"
                                else -> "🔒"
                            },
                            fontSize = 20.sp
                        )
                    }
                    DropdownMenu(
                        expanded = showModeMenu,
                        onDismissRequest = { viewModel.dismissModeMenu() }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🔒 默认模式") },
                            onClick = { viewModel.dismissModeMenu() }
                        )
                        DropdownMenuItem(
                            text = { Text("✏️ 信任编辑") },
                            onClick = { viewModel.dismissModeMenu() }
                        )
                        DropdownMenuItem(
                            text = { Text("🛡️ 绕过模式") },
                            onClick = { viewModel.dismissModeMenu() }
                        )
                    }
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("输入消息...", style = YepType.body, color = MaterialTheme.colorScheme.outline) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    maxLines = 4
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(api, sessionId, inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) Tint else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "发送",
                        tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Permission approval sheet
        if (pendingInput != null) {
            val currentInput = pendingInput!!
            ModalBottomSheet(
                onDismissRequest = { /* Non-dismissable: user must approve or deny */ },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            when (currentInput.toolName) {
                                "Bash" -> "⚙️"
                                "Edit", "Write" -> "📝"
                                else -> "🔧"
                            },
                            fontSize = 28.sp
                        )
                        Column {
                            Text(
                                currentInput.toolName,
                                style = YepType.headline,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                when (currentInput.toolName) {
                                    "Bash" -> "命令执行"
                                    "Edit" -> "文件编辑"
                                    "Write" -> "文件写入"
                                    else -> "工具调用"
                                },
                                style = YepType.caption1,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Tool input content
                    val toolInputText = when (val input = currentInput.toolInput) {
                        is Map<*, *> -> {
                            when (currentInput.toolName) {
                                "Bash" -> input["command"] as? String ?: input.toString()
                                "Edit" -> {
                                    val filePath = input["file_path"] as? String ?: ""
                                    "编辑: $filePath"
                                }
                                else -> input.toString()
                            }
                        }
                        is String -> input
                        else -> currentInput.prompt
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            toolInputText,
                            style = YepType.body,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // File path if available
                    val filePath = (currentInput.toolInput as? Map<*, *>)?.get("file_path") as? String
                    if (filePath != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "📄 $filePath",
                            style = YepType.caption1,
                            color = Tint
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Buttons
                    Button(
                        onClick = { viewModel.approveInput() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("✓ 允许", modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.approveAndAcceptEdits() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("允许并信任编辑", color = Tint)
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.denyInput() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("✗ 拒绝", modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
