package com.example.friendzone.data.remote.api

import com.example.friendzone.data.remote.dto.EventParticipantDto
import com.example.friendzone.data.remote.dto.LocationUpdateResponseDto
import com.example.friendzone.data.remote.dto.ParticipantWithUserDto
import com.example.friendzone.data.remote.dto.UpdateLocationRequest
import com.example.friendzone.data.remote.dto.UpdateSharingRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LocationsApi {
    @PATCH("events/{eventId}/sharing")
    suspend fun updateSharing(
        @Path("eventId") eventId: String,
        @Body body: UpdateSharingRequest,
    ): EventParticipantDto

    @POST("events/{eventId}/location")
    suspend fun updateLocation(
        @Path("eventId") eventId: String,
        @Body body: UpdateLocationRequest,
    ): LocationUpdateResponseDto

    @GET("events/{eventId}/participants")
    suspend fun getParticipants(
        @Path("eventId") eventId: String,
    ): List<ParticipantWithUserDto>
}
