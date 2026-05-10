package com.yepanywhere.app.data.model

import com.google.gson.annotations.SerializedName

data class Message(
    val id: String,
    val role: MessageRole = MessageRole.ASSISTANT,
    val content: Any? = null,
    val timestamp: String = "",
    @SerializedName("isStreaming")
    val isStreaming: Boolean = false,
)

enum class MessageRole {
    @SerializedName("user") USER,
    @SerializedName("assistant") ASSISTANT,
    @SerializedName("system") SYSTEM,
    @SerializedName("developer") DEVELOPER
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
