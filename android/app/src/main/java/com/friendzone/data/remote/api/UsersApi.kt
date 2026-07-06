package com.example.friendzone.data.remote.api

import com.example.friendzone.data.remote.dto.UpdateFcmTokenRequest
import com.example.friendzone.data.remote.dto.UpdateLocationSharingRequest
import com.example.friendzone.data.remote.dto.UpdateProfileRequest
import com.example.friendzone.data.remote.dto.UserDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface UsersApi {
    @GET("users/me")
    suspend fun getMe(): UserDto

    @GET("users/me/friends")
    suspend fun getMyFriends(): List<UserDto>

    @PATCH("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserDto

    @PATCH("users/me/location-sharing")
    suspend fun updateLocationSharing(@Body body: UpdateLocationSharingRequest): UserDto

    @PUT("users/me/fcm-token")
    suspend fun updateFcmToken(@Body body: UpdateFcmTokenRequest): UserDto

    @Multipart
    @POST("users/me/profile-picture")
    suspend fun uploadProfilePicture(@Part picture: MultipartBody.Part): UserDto

    @DELETE("users/me/profile-picture")
    suspend fun deleteProfilePicture(): UserDto

    @GET("users/search")
    suspend fun search(@Query("q") query: String): List<UserDto>

    @GET("users/lookup")
    suspend fun lookup(@Query("q") query: String): UserDto
}
