package com.yepanywhere.app.data.model

data class Message(
    val id: String,
    val role: MessageRole,
    val content: Any,
    val timestamp: String,
    val isStreaming: Boolean = false,
)

enum class MessageRole { USER, ASSISTANT, SYSTEM }

data class ContentBlock(
    val type: String,
    val text: String? = null,
    val name: String? = null,
    val input: Any? = null,
    val toolUseId: String? = null,
    val content: Any? = null,
    val isError: Boolean = false,
)
