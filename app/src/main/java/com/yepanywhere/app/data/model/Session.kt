package com.yepanywhere.app.data.model

import com.google.gson.annotations.SerializedName

data class SessionSummary(
    val id: String,
    val projectId: String,
    val title: String?,
    val createdAt: String,
    val updatedAt: String,
    val messageCount: Int,
    val provider: String,
    val model: String?,
    val activity: AgentActivity?,
    val pendingInputType: PendingInputType?,
    val hasUnread: Boolean?,
    val contextUsage: ContextUsage?,
)

data class Session(
    val id: String,
    val projectId: String,
    val title: String?,
    val provider: String,
    val model: String?,
    val activity: AgentActivity?,
)

data class SessionDetail(
    val session: Session,
    val messages: List<Message>,
    val pagination: Pagination?,
)

data class Pagination(
    val hasMore: Boolean?,
    val oldestMessageId: String?,
)

enum class AgentActivity {
    @SerializedName("in_turn") IN_TURN,
    @SerializedName("idle") IDLE,
    @SerializedName("waiting_input") WAITING_INPUT,
    @SerializedName("hold") HOLD,
    @SerializedName("terminated") TERMINATED
}

enum class PendingInputType {
    @SerializedName("tool_approval") TOOL_APPROVAL,
    @SerializedName("user_question") USER_QUESTION
}

data class ContextUsage(
    val inputTokens: Int,
    val percentage: Int,
    val contextWindow: Int?,
)
