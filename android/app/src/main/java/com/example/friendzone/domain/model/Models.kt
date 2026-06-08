package com.example.friendzone.domain.model

data class User(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val fcmToken: String?,
    val locationSharingEnabled: Boolean,
    val createdAt: String,
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)

enum class EventStatus {
    SCHEDULED,
    ACTIVE,
    COMPLETED,
    CANCELLED,
}

data class Event(
    val id: String,
    val organizerId: String,
    val title: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val status: EventStatus,
    val arrivalThresholdM: Int,
    val startsAt: String,
    val completedAt: String?,
    val createdAt: String,
)

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}

data class Invitation(
    val id: String,
    val eventId: String,
    val inviteeId: String,
    val invitedById: String,
    val status: InvitationStatus,
    val createdAt: String,
)

enum class ParticipantRole {
    ORGANIZER,
    PARTICIPANT,
}

data class EventParticipant(
    val id: String,
    val eventId: String,
    val userId: String,
    val role: ParticipantRole,
    val sharingLocation: Boolean,
    val arrived: Boolean,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
    val lastLocationAt: String?,
    val arrivedAt: String?,
    val createdAt: String,
)

data class ParticipantWithUser(
    val participant: EventParticipant,
    val user: User,
)

data class ProximityResult(
    val arrived: Boolean,
    val eventCompleted: Boolean,
)

data class LocationUpdateResult(
    val participant: EventParticipant,
    val proximity: ProximityResult,
)
