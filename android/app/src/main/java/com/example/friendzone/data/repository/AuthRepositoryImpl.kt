package com.example.friendzone.data.repository

import com.example.friendzone.data.local.TokenManager
import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.AuthApi
import com.example.friendzone.data.remote.dto.LoginRequest
import com.example.friendzone.data.remote.dto.RefreshTokenRequest
import com.example.friendzone.data.remote.dto.RegisterRequest
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.AuthSession
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.result.ApiResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
) : AuthRepository {
    override val isLoggedIn: Flow<Boolean> = tokenManager.isLoggedIn()

    override suspend fun register(
        email: String,
        username: String,
        password: String,
        displayName: String,
    ): ApiResult<AuthSession> = safeApiCall {
        authApi.register(RegisterRequest(email, username, password, displayName))
    }.let { result ->
        when (result) {
            is ApiResult.Success -> {
                val session = DtoMapper.toAuthSession(result.data)
                tokenManager.saveSession(
                    session.accessToken,
                    session.refreshToken,
                    session.user.id,
                )
                ApiResult.Success(session)
            }
            is ApiResult.Error -> result
            ApiResult.Loading -> ApiResult.Loading
        }
    }

    override suspend fun login(emailOrUsername: String, password: String): ApiResult<AuthSession> =
        safeApiCall {
            authApi.login(LoginRequest(emailOrUsername, password))
        }.let { result ->
            when (result) {
                is ApiResult.Success -> {
                    val session = DtoMapper.toAuthSession(result.data)
                    tokenManager.saveSession(
                        session.accessToken,
                        session.refreshToken,
                        session.user.id,
                    )
                    ApiResult.Success(session)
                }
                is ApiResult.Error -> result
                ApiResult.Loading -> ApiResult.Loading
            }
        }

    override suspend fun logout(): ApiResult<Unit> {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken != null) {
            safeApiCall { authApi.logout(RefreshTokenRequest(refreshToken)) }
        }
        tokenManager.clearSession()
        return ApiResult.Success(Unit)
    }
}
