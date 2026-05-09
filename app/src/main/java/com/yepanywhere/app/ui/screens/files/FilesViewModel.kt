package com.yepanywhere.app.ui.screens.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yepanywhere.app.data.model.FileEntry
import com.yepanywhere.app.data.remote.ApiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FilesViewModel : ViewModel() {

    private val _files = MutableStateFlow<List<FileEntry>>(emptyList())
    val files: StateFlow<List<FileEntry>> = _files

    private val _currentPath = MutableStateFlow("")
    val currentPath: StateFlow<String> = _currentPath

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadFiles(api: ApiService, projectId: String, path: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _files.value = api.getFiles(projectId, path)
                _currentPath.value = path
            } catch (_: Exception) {} finally {
                _isLoading.value = false
            }
        }
    }
}
