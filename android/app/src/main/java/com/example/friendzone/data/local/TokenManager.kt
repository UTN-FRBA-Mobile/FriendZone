package com.example.friendzone.data.local

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val tokenStore: TokenStore,
) {
    suspend fun getAccessToken(): String? = tokenStore.getAccessToken()

    suspend fun getRefreshToken(): String? = tokenStore.getRefreshToken()

    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String) {
        tokenStore.saveSession(accessToken, refreshToken, userId)
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        tokenStore.updateTokens(accessToken, refreshToken)
    }

    suspend fun clearSession() {
        tokenStore.clearSession()
    }

    fun isLoggedIn() = tokenStore.isLoggedIn
}
