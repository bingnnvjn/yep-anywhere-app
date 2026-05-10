package com.yepanywhere.app.data.remote

import com.yepanywhere.app.data.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(
    private val settingsStore: SettingsStore,
    private val baseUrl: String
) : Interceptor, CookieJar {

    private val cookieStore = mutableMapOf<String, List<Cookie>>()
    private var loginAttempted = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Add required header to all requests
        val request = originalRequest.newBuilder()
            .header("X-Yep-Anywhere", "true")
            .build()

        val response = chain.proceed(request)

        // If 401 and haven't tried login yet, try to login then retry
        if (response.code == 401 && !loginAttempted) {
            loginAttempted = true
            val password = runBlocking { settingsStore.password.first() }
            if (password.isNotBlank() && login(password)) {
                // Login succeeded, retry original request
                response.close()
                val retryRequest = originalRequest.newBuilder()
                    .header("X-Yep-Anywhere", "true")
                    .build()
                return chain.proceed(retryRequest)
            }
        }

        return response
    }

    private fun login(password: String): Boolean {
        val client = OkHttpClient.Builder()
            .cookieJar(this)
            .build()

        val body = """{"password":"$password"}""".toRequestBody("application/json".toMediaTypeOrNull()!!)
        val request = Request.Builder()
            .url("${baseUrl}api/auth/login")
            .header("X-Yep-Anywhere", "true")
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            response.close()
            success
        } catch (e: Exception) {
            false
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }
}
