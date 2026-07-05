package com.example.friendzone.data.repository

import com.example.friendzone.data.local.CacheKeys
import com.example.friendzone.data.local.LocalCacheManager
import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.FriendsApi
import com.example.friendzone.data.remote.dto.AddFriendByInviteBody
import com.example.friendzone.data.remote.dto.CreateFriendRequestBody
import com.example.friendzone.data.remote.dto.RespondFriendRequestBody
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.FriendRequest
import com.example.friendzone.domain.model.FriendRequestStatus
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.result.ApiResult
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepositoryImpl @Inject constructor(
    private val friendsApi: FriendsApi,
    private val cacheManager: LocalCacheManager,
) : FriendRepository {
    override suspend fun getCachedFriends(): List<User>? =
        cacheManager.getList(CacheKeys.FRIENDS, serializer())

    override suspend fun getCachedIncomingRequests(): List<FriendRequest>? =
        cacheManager.getList(CacheKeys.FRIEND_REQUESTS, serializer())

    override suspend fun getFriends(): ApiResult<List<User>> {
        val result = safeApiCall {
            friendsApi.getFriends().map(DtoMapper::toUser)
        }
        return when (result) {
            is ApiResult.Success -> {
                cacheManager.putList(CacheKeys.FRIENDS, result.data, serializer())
                result
            }
            is ApiResult.Error -> getCachedFriends()?.let { ApiResult.Success(it) } ?: result
            ApiResult.Loading -> result
        }
    }

    override suspend fun getIncomingRequests(): ApiResult<List<FriendRequest>> {
        val result = safeApiCall {
            friendsApi.getIncomingRequests().map(DtoMapper::toFriendRequest)
        }
        return when (result) {
            is ApiResult.Success -> {
                cacheManager.putList(CacheKeys.FRIEND_REQUESTS, result.data, serializer())
                result
            }
            is ApiResult.Error -> getCachedIncomingRequests()?.let { ApiResult.Success(it) } ?: result
            ApiResult.Loading -> result
        }
    }

    override suspend fun getPendingIncomingCount(): ApiResult<Int> = safeApiCall {
        friendsApi.getPendingIncomingCount().count
    }

    override suspend fun sendRequest(emailOrUsername: String): ApiResult<FriendRequest> =
        safeApiCall {
            DtoMapper.toFriendRequest(
                friendsApi.sendRequest(CreateFriendRequestBody(emailOrUsername)),
            )
        }.also { result ->
            if (result is ApiResult.Success) {
                invalidateFriendsCache()
            }
        }

    override suspend fun addByInvite(username: String): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(friendsApi.addByInvite(AddFriendByInviteBody(username)))
    }.also { result ->
        if (result is ApiResult.Success) {
            invalidateFriendsCache()
        }
    }

    override suspend fun respondToRequest(
        requestId: String,
        status: FriendRequestStatus,
    ): ApiResult<Unit> = safeApiCall {
        friendsApi.respondToRequest(
            requestId,
            RespondFriendRequestBody(DtoMapper.friendRequestStatusToApi(status)),
        )
        Unit
    }.also { result ->
        if (result is ApiResult.Success) {
            invalidateFriendsCache()
        }
    }

    private suspend fun invalidateFriendsCache() {
        cacheManager.delete(CacheKeys.FRIENDS)
        cacheManager.delete(CacheKeys.FRIEND_REQUESTS)
    }
}
