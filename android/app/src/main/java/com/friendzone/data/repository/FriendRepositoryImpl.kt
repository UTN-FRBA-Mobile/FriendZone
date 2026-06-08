package com.example.friendzone.data.repository

import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.FriendsApi
import com.example.friendzone.data.remote.dto.CreateFriendRequestBody
import com.example.friendzone.data.remote.dto.RespondFriendRequestBody
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.FriendRequest
import com.example.friendzone.domain.model.FriendRequestStatus
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.result.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepositoryImpl @Inject constructor(
    private val friendsApi: FriendsApi,
) : FriendRepository {
    override suspend fun getFriends(): ApiResult<List<User>> = safeApiCall {
        friendsApi.getFriends().map(DtoMapper::toUser)
    }

    override suspend fun getIncomingRequests(): ApiResult<List<FriendRequest>> = safeApiCall {
        friendsApi.getIncomingRequests().map(DtoMapper::toFriendRequest)
    }

    override suspend fun getPendingIncomingCount(): ApiResult<Int> = safeApiCall {
        friendsApi.getPendingIncomingCount().count
    }

    override suspend fun sendRequest(emailOrUsername: String): ApiResult<FriendRequest> =
        safeApiCall {
            DtoMapper.toFriendRequest(
                friendsApi.sendRequest(CreateFriendRequestBody(emailOrUsername)),
            )
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
    }
}
