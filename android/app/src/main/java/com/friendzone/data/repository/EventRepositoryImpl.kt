package com.example.friendzone.data.repository

import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.EventsApi
import com.example.friendzone.data.remote.dto.CreateEventRequest
import com.example.friendzone.data.remote.dto.UpdateEventRequest
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.result.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventsApi: EventsApi,
) : EventRepository {
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
    }

    override suspend fun getMine(): ApiResult<List<Event>> = safeApiCall {
        eventsApi.getMine().map(DtoMapper::toEvent)
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
    }

    override suspend fun delete(id: String): ApiResult<Unit> = safeApiCall {
        eventsApi.delete(id)
        Unit
    }

    override suspend fun leave(id: String): ApiResult<Unit> = safeApiCall {
        eventsApi.leave(id)
        Unit
    }
}
