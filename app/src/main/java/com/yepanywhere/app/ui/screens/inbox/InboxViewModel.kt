package com.yepanywhere.app.ui.screens.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yepanywhere.app.data.model.InboxItem
import com.yepanywhere.app.data.model.Project
import com.yepanywhere.app.data.remote.ApiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InboxViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<InboxItem>>(emptyList())
    val items: StateFlow<List<InboxItem>> = _items

    private val _allSessions = MutableStateFlow<List<InboxItem>>(emptyList())
    val allSessions: StateFlow<List<InboxItem>> = _allSessions

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadInbox(api: ApiService) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val inbox = api.getInbox()
                _items.value = inbox.needsAttention + inbox.active + inbox.recentActivity
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllSessions(api: ApiService) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val projects = api.getProjects()
                _projects.value = projects
                val allItems = mutableListOf<InboxItem>()
                for (project in projects) {
                    try {
                        val sessions = api.getSessions(project.id)
                        allItems.addAll(sessions.map { s ->
                            InboxItem(
                                sessionId = s.id,
                                projectId = s.projectId,
                                projectName = project.name,
                                sessionTitle = s.title ?: "未命名会话",
                                updatedAt = s.updatedAt,
                                pendingInputType = s.pendingInputType,
                                activity = s.activity,
                                hasUnread = s.hasUnread
                            )
                        })
                    } catch (_: Exception) {}
                }
                _allSessions.value = allItems.sortedByDescending { it.updatedAt }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSession(api: ApiService, projectId: String, title: String, onResult: (String, String, String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = api.createSessionOnly(projectId)
                val sessionId = result["sessionId"] as? String ?: return@launch
                onResult(projectId, sessionId, title)
            } catch (e: Exception) {
                _error.value = "创建会话失败: ${e.message}"
            }
        }
    }
}
