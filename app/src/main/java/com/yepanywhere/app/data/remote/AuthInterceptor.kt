package com.yepanywhere.app.data.remote

import com.yepanywhere.app.data.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class AuthInterceptor(
    private val settingsStore: SettingsStore,
    private val baseUrl: String
) : Interceptor, CookieJar {

    private val cookieStore = mutableMapOf<String, List<Cookie>>()
    private var isLoggedIn = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // If we get 401 and haven't logged in yet, try to login
        if (response.code == 401 && !isLoggedIn) {
            response.close()
            val password = runBlocking { settingsStore.password.first() }
            if (password.isNotBlank()) {
                val loginSuccess = login(password)
                if (loginSuccess) {
                    // Retry the original request with cookies
                    return chain.proceed(request)
                }
            }
        }

        return response
    }

    private fun login(password: String): Boolean {
        val client = OkHttpClient.Builder()
            .cookieJar(this)
            .build()

        val body = """{"password":"$password"}""".toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${baseUrl}auth/login")
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            response.close()
            isLoggedIn = success
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
