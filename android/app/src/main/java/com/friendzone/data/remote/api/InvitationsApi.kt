package com.example.friendzone.data.remote.api

import com.example.friendzone.data.remote.dto.CreateInvitationRequest
import com.example.friendzone.data.remote.dto.InvitationDto
import com.example.friendzone.data.remote.dto.PendingInvitationDto
import com.example.friendzone.data.remote.dto.UpdateInvitationRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface InvitationsApi {
    @GET("invitations/mine")
    suspend fun getMinePending(): List<PendingInvitationDto>

    @POST("events/{eventId}/invitations")
    suspend fun create(
        @Path("eventId") eventId: String,
        @Body body: CreateInvitationRequest,
    ): InvitationDto

    @GET("events/{eventId}/invitations")
    suspend fun getByEvent(@Path("eventId") eventId: String): List<InvitationDto>

    @PATCH("invitations/{id}")
    suspend fun respond(
        @Path("id") id: String,
        @Body body: UpdateInvitationRequest,
    ): InvitationDto
}
