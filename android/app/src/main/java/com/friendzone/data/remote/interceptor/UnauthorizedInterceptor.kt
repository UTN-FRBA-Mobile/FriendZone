package com.example.friendzone.data.remote.interceptor

import com.example.friendzone.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnauthorizedInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401 && !request.url.encodedPath.isAuthEndpoint()) {
            runBlocking { tokenManager.clearSession() }
        }
        return response
    }

    private fun String.isAuthEndpoint(): Boolean =
        endsWith("/auth/login") ||
            endsWith("/auth/register") ||
            endsWith("/auth/refresh")
}
