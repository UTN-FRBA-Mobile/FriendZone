package com.example.friendzone.data.repository

import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.LocationsApi
import com.example.friendzone.data.remote.dto.UpdateLocationRequest
import com.example.friendzone.data.remote.dto.UpdateSharingRequest
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.EventParticipant
import com.example.friendzone.domain.model.LocationUpdateResult
import com.example.friendzone.domain.model.ParticipantWithUser
import com.example.friendzone.domain.repository.LocationRepository
import com.example.friendzone.domain.result.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationsApi: LocationsApi,
) : LocationRepository {
    override suspend fun updateSharing(eventId: String, enabled: Boolean): ApiResult<EventParticipant> =
        safeApiCall {
            DtoMapper.toEventParticipant(
                locationsApi.updateSharing(eventId, UpdateSharingRequest(enabled)),
            )
        }

    override suspend fun updateLocation(
        eventId: String,
        latitude: Double,
        longitude: Double,
    ): ApiResult<LocationUpdateResult> = safeApiCall {
        DtoMapper.toLocationUpdateResult(
            locationsApi.updateLocation(eventId, UpdateLocationRequest(latitude, longitude)),
        )
    }

    override suspend fun getParticipants(eventId: String): ApiResult<List<ParticipantWithUser>> =
        safeApiCall {
            locationsApi.getParticipants(eventId).map(DtoMapper::toParticipantWithUser)
        }
}
