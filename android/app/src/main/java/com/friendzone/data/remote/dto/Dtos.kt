package com.example.friendzone.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val displayName: String,
)

@Serializable
data class LoginRequest(
    val emailOrUsername: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class AuthUserDto(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val locationSharingEnabled: Boolean,
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUserDto,
)

@Serializable
data class LogoutResponseDto(
    val success: Boolean,
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val fcmToken: String? = null,
    val locationSharingEnabled: Boolean,
    val createdAt: String,
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String,
)

@Serializable
data class UpdateLocationSharingRequest(
    val enabled: Boolean,
)

@Serializable
data class UpdateFcmTokenRequest(
    val token: String,
)

@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val startsAt: String,
    val arrivalThresholdM: Int? = null,
    val trackingLeadMinutes: Int? = null,
)

@Serializable
data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val startsAt: String? = null,
    val arrivalThresholdM: Int? = null,
    val trackingLeadMinutes: Int? = null,
    val status: String? = null,
)

@Serializable
data class EventDto(
    val id: String,
    val organizerId: String,
    val title: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val status: String,
    val arrivalThresholdM: Int,
    val trackingLeadMinutes: Int = 30,
    val startsAt: String,
    val completedAt: String? = null,
    val createdAt: String,
    val coverImageUrl: String? = null,
)

@Serializable
data class PendingInvitationDto(
    val id: String,
    val eventId: String,
    val inviteeId: String,
    val invitedById: String,
    val status: String,
    val createdAt: String,
    val eventTitle: String,
    val eventStartsAt: String,
    val organizerDisplayName: String,
)

@Serializable
data class CreateInvitationRequest(
    val emailOrUsername: String,
)

@Serializable
data class UpdateInvitationRequest(
    val status: String,
)

@Serializable
data class InvitationDto(
    val id: String,
    val eventId: String,
    val inviteeId: String,
    val invitedById: String,
    val status: String,
    val createdAt: String,
)

@Serializable
data class UpdateLocationRequest(
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class UpdateSharingRequest(
    val enabled: Boolean,
)

@Serializable
data class EventParticipantDto(
    val id: String,
    val eventId: String,
    val userId: String,
    val role: String,
    val sharingLocation: Boolean,
    val arrived: Boolean,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null,
    val lastLocationAt: String? = null,
    val arrivedAt: String? = null,
    val createdAt: String,
)

@Serializable
data class ProximityResultDto(
    val arrived: Boolean,
    val eventCompleted: Boolean,
)

@Serializable
data class LocationUpdateResponseDto(
    val participant: EventParticipantDto,
    val proximity: ProximityResultDto,
)

@Serializable
data class ParticipantWithUserDto(
    val id: String,
    val eventId: String,
    val userId: String,
    val role: String,
    val sharingLocation: Boolean,
    val arrived: Boolean,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null,
    val lastLocationAt: String? = null,
    val arrivedAt: String? = null,
    val createdAt: String,
    val user: UserDto,
)

@Serializable
data class CreateFriendRequestBody(
    val emailOrUsername: String,
)

@Serializable
data class RespondFriendRequestBody(
    val status: String,
)

@Serializable
data class FriendRequestDto(
    val id: String,
    val requesterId: String,
    val addresseeId: String,
    val status: String,
    val createdAt: String,
    val respondedAt: String? = null,
    val requester: UserDto,
)

@Serializable
data class FriendRequestCountDto(
    val count: Int,
)

@Serializable
data class SuccessResponseDto(
    val success: Boolean,
)

@Serializable
data class ApiErrorDto(
    val statusCode: Int,
    val message: String,
    val timestamp: String? = null,
)

@Serializable
data class InboxNotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val createdAt: String,
    val actionable: Boolean,
    val read: Boolean,
    val data: Map<String, String> = emptyMap(),
)

@Serializable
data class NotificationBadgeCountDto(
    val count: Int,
)
