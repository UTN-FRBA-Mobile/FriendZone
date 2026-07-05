package com.example.friendzone.data.remote.api

import com.example.friendzone.data.remote.dto.AddFriendByInviteBody
import com.example.friendzone.data.remote.dto.CreateFriendRequestBody
import com.example.friendzone.data.remote.dto.FriendRequestCountDto
import com.example.friendzone.data.remote.dto.FriendRequestDto
import com.example.friendzone.data.remote.dto.RespondFriendRequestBody
import com.example.friendzone.data.remote.dto.SuccessResponseDto
import com.example.friendzone.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface FriendsApi {
    @GET("friends")
    suspend fun getFriends(): List<UserDto>

    @GET("friends/requests")
    suspend fun getIncomingRequests(): List<FriendRequestDto>

    @GET("friends/requests/count")
    suspend fun getPendingIncomingCount(): FriendRequestCountDto

    @POST("friends/requests")
    suspend fun sendRequest(@Body body: CreateFriendRequestBody): FriendRequestDto

    @POST("friends/add-by-invite")
    suspend fun addByInvite(@Body body: AddFriendByInviteBody): UserDto

    @PATCH("friends/requests/{id}")
    suspend fun respondToRequest(
        @Path("id") requestId: String,
        @Body body: RespondFriendRequestBody,
    ): SuccessResponseDto
}
