package com.example.friendzone.data.local

import com.example.friendzone.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val tokenStore: TokenStore,
) {
    val isLoggedIn: Flow<Boolean> = tokenStore.isLoggedIn

    val currentUser: Flow<User?> = tokenStore.currentUser

    suspend fun getAccessToken(): String? = tokenStore.getAccessToken()

    suspend fun getRefreshToken(): String? = tokenStore.getRefreshToken()

    suspend fun saveSession(accessToken: String, refreshToken: String, user: User) {
        tokenStore.saveSession(accessToken, refreshToken, user)
    }

    suspend fun saveUserProfile(user: User) {
        tokenStore.saveUserProfile(user)
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        tokenStore.updateTokens(accessToken, refreshToken)
    }

    suspend fun clearSession() {
        tokenStore.clearSession()
    }
}
