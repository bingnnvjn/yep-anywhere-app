package com.yepanywhere.app.data.remote

import com.yepanywhere.app.data.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val settingsStore: SettingsStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val password = runBlocking { settingsStore.password.first() }
        val request = if (password.isNotBlank()) {
            chain.request().newBuilder()
                .header("Authorization", Credentials.basic("", password))
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
