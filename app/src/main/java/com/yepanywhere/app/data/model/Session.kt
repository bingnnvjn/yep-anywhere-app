package com.yepanywhere.app.data.model

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

enum class AgentActivity { IN_TURN, IDLE, WAITING_INPUT, HOLD, TERMINATED }
enum class PendingInputType { TOOL_APPROVAL, USER_QUESTION }

data class ContextUsage(
    val inputTokens: Int,
    val percentage: Int,
    val contextWindow: Int?,
)
