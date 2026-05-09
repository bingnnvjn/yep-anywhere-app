package com.yepanywhere.app.data.remote

import com.yepanywhere.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<Unit>

    @POST("/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("/auth/status")
    suspend fun getAuthStatus(): Map<String, Any>

    @GET("/api/inbox")
    suspend fun getInbox(): InboxResponse

    @GET("/api/projects")
    suspend fun getProjects(): List<Project>

    @GET("/api/projects/{projectId}/sessions")
    suspend fun getSessions(@Path("projectId") projectId: String): List<SessionSummary>

    @GET("/api/projects/{projectId}/sessions/{sessionId}")
    suspend fun getSession(
        @Path("projectId") projectId: String,
        @Path("sessionId") sessionId: String
    ): Session

    @POST("/api/sessions/{sessionId}/messages")
    suspend fun sendMessage(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, String>
    ): Response<Unit>

    @POST("/api/sessions/{sessionId}/input")
    suspend fun submitInput(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, Any>
    ): Response<Unit>

    @GET("/api/sessions/{sessionId}/pending-input")
    suspend fun getPendingInput(
        @Path("sessionId") sessionId: String
    ): Map<String, Any>?

    @GET("/api/sessions/{sessionId}/process")
    suspend fun getProcessState(
        @Path("sessionId") sessionId: String
    ): Map<String, Any>

    @POST("/api/projects/{projectId}/sessions")
    suspend fun createSession(
        @Path("projectId") projectId: String,
        @Body body: Map<String, String>
    ): Session

    @GET("/api/projects/{projectId}/files")
    suspend fun getFiles(
        @Path("projectId") projectId: String,
        @Query("path") path: String = ""
    ): List<FileEntry>

    @GET("/api/projects/{projectId}/files/raw")
    suspend fun getFileContent(
        @Path("projectId") projectId: String,
        @Query("path") path: String
    ): Response<String>

    @GET("/api/projects/{projectId}/git")
    suspend fun getGitStatus(
        @Path("projectId") projectId: String
    ): Map<String, Any>

    @GET("/api/status")
    suspend fun getServerStatus(): Map<String, Any>
}
