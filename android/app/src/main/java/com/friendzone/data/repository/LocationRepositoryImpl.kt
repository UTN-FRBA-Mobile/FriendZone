package com.example.friendzone.data.repository

import com.example.friendzone.data.local.CacheKeys
import com.example.friendzone.data.local.LocalCacheManager
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
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationsApi: LocationsApi,
    private val cacheManager: LocalCacheManager,
) : LocationRepository {
    override suspend fun getCachedParticipants(eventId: String): List<ParticipantWithUser>? =
        cacheManager.getList(CacheKeys.eventParticipants(eventId), serializer())

    override suspend fun updateSharing(eventId: String, enabled: Boolean): ApiResult<EventParticipant> =
        safeApiCall {
            DtoMapper.toEventParticipant(
                locationsApi.updateSharing(eventId, UpdateSharingRequest(enabled)),
            )
        }.also { result ->
            if (result is ApiResult.Success) {
                cacheManager.delete(CacheKeys.eventParticipants(eventId))
            }
        }

    override suspend fun updateLocation(
        eventId: String,
        latitude: Double,
        longitude: Double,
    ): ApiResult<LocationUpdateResult> = safeApiCall {
        DtoMapper.toLocationUpdateResult(
            locationsApi.updateLocation(eventId, UpdateLocationRequest(latitude, longitude)),
        )
    }.also { result ->
        if (result is ApiResult.Success) {
            cacheManager.delete(CacheKeys.eventParticipants(eventId))
        }
    }

    override suspend fun getParticipants(eventId: String): ApiResult<List<ParticipantWithUser>> {
        val result = safeApiCall {
            locationsApi.getParticipants(eventId).map(DtoMapper::toParticipantWithUser)
        }
        return when (result) {
            is ApiResult.Success -> {
                cacheManager.putList(CacheKeys.eventParticipants(eventId), result.data, serializer())
                result
            }
            is ApiResult.Error -> getCachedParticipants(eventId)?.let { ApiResult.Success(it) } ?: result
            ApiResult.Loading -> result
        }
    }
}
