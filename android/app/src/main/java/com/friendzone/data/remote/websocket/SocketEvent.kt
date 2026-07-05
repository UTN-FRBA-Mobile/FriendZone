package com.example.friendzone.data.remote.websocket

import kotlinx.serialization.Serializable

enum class SocketEventType {
    LOCATION_UPDATED,
    PARTICIPANT_JOINED,
    PARTICIPANT_ARRIVED,
    EVENT_COMPLETED,
    EVENT_DELETED,
    PARTICIPANT_LEFT,
    RECONNECTED,
}

@Serializable
data class LocationUpdatedPayload(
    val userId: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: String,
)

@Serializable
data class ParticipantJoinedPayload(
    val userId: String,
    val displayName: String,
)

@Serializable
data class ParticipantArrivedPayload(
    val userId: String,
    val displayName: String,
    val arrivedAt: String,
)

@Serializable
data class EventCompletedPayload(
    val completedAt: String,
)

@Serializable
data class EventDeletedPayload(
    val reason: String? = null,
)

@Serializable
data class ParticipantLeftPayload(
    val userId: String,
    val displayName: String,
)

data class SocketEvent(
    val type: SocketEventType,
    val eventId: String,
    val rawPayload: String,
)
