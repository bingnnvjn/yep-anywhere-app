package com.yepanywhere.app.data.model

import com.google.gson.annotations.SerializedName

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

enum class FileStatus {
    @SerializedName("new") NEW,
    @SerializedName("modified") MODIFIED,
    @SerializedName("deleted") DELETED
}
