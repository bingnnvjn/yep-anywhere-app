package com.yepanywhere.app.data.model

data class Project(
    val id: String,
    val name: String,
    val path: String,
)

data class FileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long?,
    val status: FileStatus?,
)

enum class FileStatus { NEW, MODIFIED, DELETED }
