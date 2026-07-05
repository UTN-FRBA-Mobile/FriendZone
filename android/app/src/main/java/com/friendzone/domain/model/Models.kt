package com.example.friendzone.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val fcmToken: String?,
    val locationSharingEnabled: Boolean,
    val createdAt: String,
)

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)

@Serializable
enum class EventStatus {
    SCHEDULED,
    ACTIVE,
    COMPLETED,
    CANCELLED,
}

@Serializable
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
    val trackingLeadMinutes: Int = 30,
    val startsAt: String,
    val completedAt: String?,
    val createdAt: String,
    val coverImageUrl: String? = null,
)

@Serializable
enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}

@Serializable
data class Invitation(
    val id: String,
    val eventId: String,
    val inviteeId: String,
    val invitedById: String,
    val status: InvitationStatus,
    val createdAt: String,
)

@Serializable
data class PendingInvitation(
    val id: String,
    val eventId: String,
    val inviteeId: String,
    val invitedById: String,
    val status: InvitationStatus,
    val createdAt: String,
    val eventTitle: String,
    val eventStartsAt: String,
    val organizerDisplayName: String,
)

@Serializable
enum class ParticipantRole {
    ORGANIZER,
    PARTICIPANT,
}

@Serializable
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

@Serializable
data class ParticipantWithUser(
    val participant: EventParticipant,
    val user: User,
)

@Serializable
enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}

@Serializable
data class FriendRequest(
    val id: String,
    val requesterId: String,
    val addresseeId: String,
    val status: FriendRequestStatus,
    val createdAt: String,
    val respondedAt: String?,
    val requester: User,
)

data class ProximityResult(
    val arrived: Boolean,
    val eventCompleted: Boolean,
)

data class LocationUpdateResult(
    val participant: EventParticipant,
    val proximity: ProximityResult,
)

@Serializable
enum class AppNotificationType {
    FRIEND_REQUEST,
    FRIEND_REQUEST_ACCEPTED,
    INVITATION_CREATED,
    PARTICIPANT_ARRIVED,
    ORGANIZER_SELF_ARRIVED,
    EVENT_COMPLETED,
    EVENT_CANCELLED,
}

@Serializable
data class InboxNotification(
    val id: String,
    val type: AppNotificationType,
    val title: String,
    val body: String,
    val createdAt: String,
    val actionable: Boolean,
    val read: Boolean,
    val data: Map<String, String>,
)
