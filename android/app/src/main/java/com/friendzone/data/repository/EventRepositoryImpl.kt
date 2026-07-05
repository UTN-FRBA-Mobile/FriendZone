package com.example.friendzone.data.repository

import com.example.friendzone.data.local.CacheKeys
import com.example.friendzone.data.local.LocalCacheManager
import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.EventsApi
import com.example.friendzone.data.remote.dto.CreateEventRequest
import com.example.friendzone.data.remote.dto.UpdateEventRequest
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.result.ApiResult
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventsApi: EventsApi,
    private val cacheManager: LocalCacheManager,
) : EventRepository {
    override suspend fun getCachedMine(): List<Event>? =
        cacheManager.getList(CacheKeys.EVENTS_MINE, serializer())

    override suspend fun create(
        title: String,
        description: String?,
        latitude: Double,
        longitude: Double,
        address: String?,
        startsAt: String,
        arrivalThresholdM: Int?,
        trackingLeadMinutes: Int,
    ): ApiResult<Event> = safeApiCall {
        DtoMapper.toEvent(
            eventsApi.create(
                CreateEventRequest(
                    title = title,
                    description = description,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    startsAt = startsAt,
                    arrivalThresholdM = arrivalThresholdM,
                    trackingLeadMinutes = trackingLeadMinutes,
                ),
            ),
        )
    }.also { result ->
        if (result is ApiResult.Success) {
            invalidateEventsCache()
        }
    }

    override suspend fun getMine(): ApiResult<List<Event>> {
        val result = safeApiCall {
            eventsApi.getMine().map(DtoMapper::toEvent)
        }
        return when (result) {
            is ApiResult.Success -> {
                cacheManager.putList(CacheKeys.EVENTS_MINE, result.data, serializer())
                result
            }
            is ApiResult.Error -> getCachedMine()?.let { ApiResult.Success(it) } ?: result
            ApiResult.Loading -> result
        }
    }

    override suspend fun getById(id: String): ApiResult<Event> = safeApiCall {
        DtoMapper.toEvent(eventsApi.getById(id))
    }

    override suspend fun update(
        id: String,
        title: String?,
        description: String?,
        latitude: Double?,
        longitude: Double?,
        address: String?,
        startsAt: String?,
        arrivalThresholdM: Int?,
    ): ApiResult<Event> = safeApiCall {
        DtoMapper.toEvent(
            eventsApi.update(
                id,
                UpdateEventRequest(
                    title = title,
                    description = description,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    startsAt = startsAt,
                    arrivalThresholdM = arrivalThresholdM,
                ),
            ),
        )
    }.also { result ->
        if (result is ApiResult.Success) {
            invalidateEventsCache()
        }
    }

    override suspend fun updateStatus(id: String, status: EventStatus): ApiResult<Event> =
        safeApiCall {
            val apiStatus = when (status) {
                EventStatus.COMPLETED -> "completed"
                EventStatus.CANCELLED -> "cancelled"
                else -> error("Unsupported status update: $status")
            }
            DtoMapper.toEvent(eventsApi.update(id, UpdateEventRequest(status = apiStatus)))
        }.also { result ->
            if (result is ApiResult.Success) {
                invalidateEventsCache()
            }
        }

    override suspend fun uploadCover(
        id: String,
        bytes: ByteArray,
        mimeType: String,
    ): ApiResult<Event> = safeApiCall {
        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("cover", "cover", requestBody)
        DtoMapper.toEvent(eventsApi.uploadCover(id, part))
    }.also { result ->
        if (result is ApiResult.Success) {
            invalidateEventsCache()
        }
    }

    override suspend fun delete(id: String): ApiResult<Unit> = safeApiCall {
        eventsApi.delete(id)
        Unit
    }.also { result ->
        if (result is ApiResult.Success) {
            invalidateEventsCache()
        }
    }

    override suspend fun leave(id: String): ApiResult<Unit> = safeApiCall {
        eventsApi.leave(id)
        Unit
    }.also { result ->
        if (result is ApiResult.Success) {
            invalidateEventsCache()
        }
    }

    private suspend fun invalidateEventsCache() {
        cacheManager.delete(CacheKeys.EVENTS_MINE)
        cacheManager.deleteByPrefix(CacheKeys.EVENT_INVITATIONS_PREFIX)
        cacheManager.deleteByPrefix(CacheKeys.EVENT_PARTICIPANTS_PREFIX)
    }
}
