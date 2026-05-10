package com.yepanywhere.app.ui.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yepanywhere.app.data.model.Message
import com.yepanywhere.app.data.remote.ApiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var savedApi: ApiService? = null
    private var savedProjectId: String? = null
    private var savedSessionId: String? = null

    fun loadSession(api: ApiService, projectId: String, sessionId: String) {
        savedApi = api
        savedProjectId = projectId
        savedSessionId = sessionId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val detail = api.getSession(projectId, sessionId)
                _messages.value = detail.messages.filter { it.type != "system" }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to load session", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(api: ApiService, sessionId: String, text: String) {
        viewModelScope.launch {
            try {
                api.sendMessage(sessionId, mapOf("message" to text))
                // Reload messages after sending
                val pid = savedProjectId
                if (pid != null) {
                    val detail = api.getSession(pid, sessionId)
                    _messages.value = detail.messages.filter { it.type != "system" }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message", e)
            }
        }
    }
}
