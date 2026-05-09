package com.yepanywhere.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.yepanywhere.app.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val serverUrl by viewModel.serverUrl.collectAsState()
    val password by viewModel.password.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()

    var editUrl by remember(serverUrl) { mutableStateOf(serverUrl) }
    var editPassword by remember(password) { mutableStateOf(password) }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Large Title
        Text("设置", style = YepType.largeTitle, color = MaterialTheme.colorScheme.onBackground)

        // 连接 Section
        SettingsSection(title = "连接") {
            SettingsTextField(
                label = "服务器地址",
                value = editUrl,
                onValueChange = { editUrl = it },
                placeholder = "http://192.168.1.100:3000",
                keyboardType = KeyboardType.Uri
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(start = 52.dp))
            SettingsTextField(
                label = "密码",
                value = editPassword,
                onValueChange = { editPassword = it },
                placeholder = "输入密码",
                isPassword = true,
                showPassword = showPassword,
                onTogglePassword = { showPassword = !showPassword }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(start = 52.dp))
            SettingsRow(
                label = "连接状态",
                value = if (serverUrl.isNotBlank()) "已配置" else "未配置",
                valueColor = if (serverUrl.isNotBlank()) Green else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.save(editUrl.trim(), editPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Tint)
            ) {
                Text("保存连接设置", style = YepType.body)
            }
            Spacer(Modifier.height(4.dp))
        }

        // 外观 Section
        SettingsSection(title = "外观") {
            DarkModeSelector(selected = darkMode, onSelect = { viewModel.setDarkMode(it) })
        }

        // 关于 Section
        SettingsSection(title = "关于") {
            SettingsRow(label = "版本", value = "2.0.0")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(start = 52.dp))
            SettingsRow(label = "构建", value = "Native Compose")
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title.uppercase(),
            style = YepType.caption1,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = YepType.body,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(80.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = YepType.body, color = MaterialTheme.colorScheme.outline) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = YepType.body,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Text(if (showPassword) "隐藏" else "显示", style = YepType.caption1, color = Tint)
                    }
                }
            } else null,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Tint,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.outline
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = YepType.body, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
        Text(value, style = YepType.body, color = valueColor)
    }
}

@Composable
private fun DarkModeSelector(selected: Int, onSelect: (Int) -> Unit) {
    val options = listOf("跟随系统" to 0, "浅色" to 1, "深色" to 2)
    options.forEach { (label, mode) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(mode) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = YepType.body, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            RadioButton(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                colors = RadioButtonDefaults.colors(selectedColor = Tint)
            )
        }
        if (mode < 2) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(start = 16.dp))
        }
    }
}
