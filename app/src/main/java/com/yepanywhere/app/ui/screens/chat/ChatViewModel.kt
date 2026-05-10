package com.yepanywhere.app.ui.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yepanywhere.app.data.model.Message
import com.yepanywhere.app.data.model.MessageBody
import com.yepanywhere.app.data.model.PendingInput
import com.yepanywhere.app.data.remote.ApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AgentStatus {
    IDLE, THINKING, WAITING_INPUT
}

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _agentStatus = MutableStateFlow(AgentStatus.IDLE)
    val agentStatus: StateFlow<AgentStatus> = _agentStatus

    private val _pendingInput = MutableStateFlow<PendingInput?>(null)
    val pendingInput: StateFlow<PendingInput?> = _pendingInput

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
                // Add user message locally immediately
                val localMsg = Message(
                    id = "local-${System.currentTimeMillis()}",
                    type = "user",
                    message = MessageBody(role = "user", content = text),
                    timestamp = java.time.Instant.now().toString()
                )
                _messages.value = _messages.value + localMsg

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
                // Start polling for Claude's reply
                _agentStatus.value = AgentStatus.THINKING
                startPolling()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message", e)
            }
        }
    }

    private fun startPolling() {
        pollJob = viewModelScope.launch {
            val maxAttempts = 60 // 3 min max
            for (i in 1..maxAttempts) {
                delay(3000)
                try {
                    refreshMessages()
                    checkAgentStatus()
                    checkPendingInput()
                    val msgs = _messages.value
                    if (msgs.isNotEmpty() && msgs.last().role != com.yepanywhere.app.data.model.MessageRole.USER) {
                        _agentStatus.value = AgentStatus.IDLE
                        break
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Poll error", e)
                }
            }
            _agentStatus.value = AgentStatus.IDLE
        }
    }

    private suspend fun checkAgentStatus() {
        try {
            val api = savedApi ?: return
            val pid = savedProjectId ?: return
            val sid = savedSessionId ?: return
            val metadata = api.getSessionMetadata(pid, sid)
            @Suppress("UNCHECKED_CAST")
            val ownership = metadata["ownership"] as? Map<String, Any> ?: return
            val state = ownership["state"] as? String
            _agentStatus.value = when (state) {
                "in_turn" -> AgentStatus.THINKING
                "waiting-input" -> AgentStatus.WAITING_INPUT
                else -> AgentStatus.IDLE
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Failed to check status", e)
        }
    }

    private suspend fun checkPendingInput() {
        try {
            val api = savedApi ?: return
            val pid = savedProjectId ?: return
            val sid = savedSessionId ?: return
            val metadata = api.getSessionMetadata(pid, sid)
            @Suppress("UNCHECKED_CAST")
            val pending = metadata["pendingInputRequest"] as? Map<String, Any>?
            if (pending != null) {
                _pendingInput.value = PendingInput(
                    id = pending["id"] as? String ?: "",
                    sessionId = pending["sessionId"] as? String ?: "",
                    type = pending["type"] as? String ?: "",
                    prompt = pending["prompt"] as? String ?: "",
                    toolName = pending["toolName"] as? String ?: "",
                    toolInput = pending["toolInput"],
                    timestamp = pending["timestamp"] as? String ?: ""
                )
            } else {
                _pendingInput.value = null
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Failed to check pending input", e)
        }
    }

    fun approveInput() {
        viewModelScope.launch {
            try {
                val api = savedApi ?: return@launch
                val sid = savedSessionId ?: return@launch
                val input = _pendingInput.value ?: return@launch
                api.submitInput(sid, mapOf(
                    "requestId" to input.id,
                    "response" to "approve"
                ))
                _pendingInput.value = null
                _agentStatus.value = AgentStatus.THINKING
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to approve input", e)
            }
        }
    }

    fun approveAndAcceptEdits() {
        viewModelScope.launch {
            try {
                val api = savedApi ?: return@launch
                val sid = savedSessionId ?: return@launch
                val input = _pendingInput.value ?: return@launch
                api.submitInput(sid, mapOf(
                    "requestId" to input.id,
                    "response" to "approve_accept_edits"
                ))
                _pendingInput.value = null
                _agentStatus.value = AgentStatus.THINKING
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to approve input", e)
            }
        }
    }

    fun denyInput() {
        viewModelScope.launch {
            try {
                val api = savedApi ?: return@launch
                val sid = savedSessionId ?: return@launch
                val input = _pendingInput.value ?: return@launch
                api.submitInput(sid, mapOf(
                    "requestId" to input.id,
                    "response" to "deny"
                ))
                _pendingInput.value = null
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to deny input", e)
            }
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
