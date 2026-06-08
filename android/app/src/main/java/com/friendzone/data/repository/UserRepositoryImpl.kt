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

    override suspend fun search(query: String): ApiResult<List<User>> = safeApiCall {
        usersApi.search(query).map(DtoMapper::toUser)
    }

    override suspend fun lookup(query: String): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(usersApi.lookup(query))
    }
}

