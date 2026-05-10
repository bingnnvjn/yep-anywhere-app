package com.yepanywhere.app.data.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("uuid")
    val id: String = "",
    val type: String = "",
    val message: MessageBody? = null,
    val timestamp: String = "",
) {
    val role: MessageRole
        get() = when (message?.role) {
            "user" -> MessageRole.USER
            "assistant" -> MessageRole.ASSISTANT
            "system" -> MessageRole.SYSTEM
            "developer" -> MessageRole.DEVELOPER
            else -> MessageRole.ASSISTANT
        }

    val content: Any?
        get() = message?.content

    val isStreaming: Boolean
        get() = false
}

data class MessageBody(
    val role: String = "assistant",
    val content: Any? = null,
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    DEVELOPER
}

data class ContentBlock(
    val type: String,
    val text: String? = null,
    val name: String? = null,
    val input: Any? = null,
    val toolUseId: String? = null,
    val content: Any? = null,
    val isError: Boolean = false,
)

data class PendingInput(
    val id: String = "",
    val sessionId: String = "",
    val type: String = "",
    val prompt: String = "",
    val toolName: String = "",
    val toolInput: Any? = null,
    val timestamp: String = ""
)
