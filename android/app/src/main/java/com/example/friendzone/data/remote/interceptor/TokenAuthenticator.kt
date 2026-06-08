package com.example.friendzone.data.remote.interceptor

import com.example.friendzone.data.local.TokenManager
import com.example.friendzone.data.remote.api.AuthApi
import com.example.friendzone.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    @Named("refresh") private val authApi: AuthApi,
) : Authenticator {
    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            runBlocking { tokenManager.clearSession() }
            return null
        }

        val newToken = runBlocking {
            refreshMutex.withLock {
                val currentToken = tokenManager.getAccessToken()
                val requestToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")
                    ?.trim()

                if (currentToken != null && currentToken != requestToken) {
                    return@withLock currentToken
                }

                val refreshToken = tokenManager.getRefreshToken() ?: return@withLock null
                val refreshed = runCatching {
                    authApi.refresh(RefreshTokenRequest(refreshToken))
                }.getOrNull() ?: return@withLock null

                tokenManager.updateTokens(refreshed.accessToken, refreshed.refreshToken)
                refreshed.accessToken
            }
        }

        return newToken?.let { token ->
            response.request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
