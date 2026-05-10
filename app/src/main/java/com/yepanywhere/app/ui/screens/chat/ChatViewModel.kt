package com.yepanywhere.app.ui.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yepanywhere.app.data.model.Message
import com.yepanywhere.app.data.remote.ApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isWaitingReply = MutableStateFlow(false)
    val isWaitingReply: StateFlow<Boolean> = _isWaitingReply

    private var savedApi: ApiService? = null
    private var savedProjectId: String? = null
    private var savedSessionId: String? = null
    private var pollJob: Job? = null

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
        pollJob?.cancel()
        viewModelScope.launch {
            try {
                val body = mapOf("message" to text)
                var response = api.sendMessage(sessionId, body)
                if (!response.isSuccessful) {
                    Log.w("ChatViewModel", "sendMessage ${response.code()}, trying resume")
                    val pid = savedProjectId
                    if (pid != null) {
                        response = api.resumeSession(pid, sessionId, body)
                    }
                }
                if (!response.isSuccessful) {
                    Log.e("ChatViewModel", "Send failed: ${response.code()} ${response.errorBody()?.string()}")
                    return@launch
                }
                // Reload once immediately
                refreshMessages()
                // Start polling for Claude's reply
                startPolling()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message", e)
            }
        }
    }

    private fun startPolling() {
        pollJob = viewModelScope.launch {
            _isWaitingReply.value = true
            val maxAttempts = 60 // 3 min max
            for (i in 1..maxAttempts) {
                delay(3000)
                try {
                    refreshMessages()
                    val msgs = _messages.value
                    if (msgs.isNotEmpty() && msgs.last().role != com.yepanywhere.app.data.model.MessageRole.USER) {
                        // Got a reply
                        break
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Poll error", e)
                }
            }
            _isWaitingReply.value = false
        }
    }

    private suspend fun refreshMessages() {
        val api = savedApi ?: return
        val pid = savedProjectId ?: return
        val sid = savedSessionId ?: return
        val detail = api.getSession(pid, sid)
        _messages.value = detail.messages.filter { it.type != "system" }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
