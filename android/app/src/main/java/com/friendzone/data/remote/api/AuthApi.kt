package com.example.friendzone.data.remote.api

import com.example.friendzone.data.remote.dto.AuthResponseDto
import com.example.friendzone.data.remote.dto.LoginRequest
import com.example.friendzone.data.remote.dto.LogoutResponseDto
import com.example.friendzone.data.remote.dto.RefreshTokenRequest
import com.example.friendzone.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshTokenRequest): LogoutResponseDto
}
