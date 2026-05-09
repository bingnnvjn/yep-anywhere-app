package com.yepanywhere.app.data.model

data class InboxResponse(
    val needsAttention: List<InboxItem>,
    val active: List<InboxItem>,
    val recentActivity: List<InboxItem>,
    val unread8h: List<InboxItem>,
    val unread24h: List<InboxItem>,
)

data class InboxItem(
    val sessionId: String,
    val projectId: String,
    val projectName: String,
    val sessionTitle: String,
    val updatedAt: String,
    val pendingInputType: PendingInputType?,
    val activity: AgentActivity?,
    val hasUnread: Boolean?,
)
