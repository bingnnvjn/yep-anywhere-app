package com.yepanywhere.app.ui.screens.chat

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

    fun loadSession(api: ApiService, projectId: String, sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val session = api.getSession(projectId, sessionId)
                _messages.value = session.messages
            } catch (_: Exception) {} finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(api: ApiService, sessionId: String, text: String) {
        viewModelScope.launch {
            try {
                api.sendMessage(sessionId, mapOf("message" to text))
                // Reload messages after sending
            } catch (_: Exception) {}
        }
    }
}
