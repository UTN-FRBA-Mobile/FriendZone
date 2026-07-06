package com.example.friendzone.data.repository

import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.UsersApi
import com.example.friendzone.data.remote.dto.UpdateFcmTokenRequest
import com.example.friendzone.data.remote.dto.UpdateLocationSharingRequest
import com.example.friendzone.data.remote.dto.UpdateProfileRequest
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.UserRepository
import com.example.friendzone.domain.result.ApiResult
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi,
) : UserRepository {
    override suspend fun getMe(): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(usersApi.getMe())
    }

    override suspend fun getFriends(): ApiResult<List<User>> = safeApiCall {
        usersApi.getMyFriends().map(DtoMapper::toUser)
    }

    override suspend fun updateProfile(displayName: String): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(usersApi.updateProfile(UpdateProfileRequest(displayName)))
    }

    override suspend fun updateLocationSharing(enabled: Boolean): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(usersApi.updateLocationSharing(UpdateLocationSharingRequest(enabled)))
    }

    override suspend fun updateFcmToken(token: String): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(usersApi.updateFcmToken(UpdateFcmTokenRequest(token)))
    }

    override suspend fun uploadProfilePicture(
        bytes: ByteArray,
        mimeType: String,
    ): ApiResult<User> = safeApiCall {
        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("picture", "picture", requestBody)
        DtoMapper.toUser(usersApi.uploadProfilePicture(part))
    }

    override suspend fun removeProfilePicture(): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(usersApi.deleteProfilePicture())
    }

    override suspend fun deleteAccount(): ApiResult<Unit> = safeApiCall {
        usersApi.deleteAccount()
        Unit
    }

    override suspend fun search(query: String): ApiResult<List<User>> = safeApiCall {
        usersApi.search(query).map(DtoMapper::toUser)
    }

    override suspend fun lookup(query: String): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(usersApi.lookup(query))
    }
}

