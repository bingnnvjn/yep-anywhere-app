package com.yepanywhere.app.ui.screens.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yepanywhere.app.data.model.InboxItem
import com.yepanywhere.app.data.remote.ApiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InboxViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<InboxItem>>(emptyList())
    val items: StateFlow<List<InboxItem>> = _items

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
}
