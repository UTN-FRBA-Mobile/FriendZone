package com.example.friendzone.data.mapper

import com.example.friendzone.data.remote.dto.AuthResponseDto
import com.example.friendzone.data.remote.dto.AuthUserDto
import com.example.friendzone.data.remote.dto.EventDto
import com.example.friendzone.data.remote.dto.EventParticipantDto
import com.example.friendzone.data.remote.dto.InvitationDto
import com.example.friendzone.data.remote.dto.LocationUpdateResponseDto
import com.example.friendzone.data.remote.dto.ParticipantWithUserDto
import com.example.friendzone.data.remote.dto.ProximityResultDto
import com.example.friendzone.data.remote.dto.UserDto
import com.example.friendzone.domain.model.AuthSession
import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventParticipant
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.model.Invitation
import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.domain.model.LocationUpdateResult
import com.example.friendzone.domain.model.ParticipantRole
import com.example.friendzone.domain.model.ParticipantWithUser
import com.example.friendzone.domain.model.ProximityResult
import com.example.friendzone.domain.model.User

object DtoMapper {
    fun toUser(dto: UserDto): User = User(
        id = dto.id,
        email = dto.email,
        username = dto.username,
        displayName = dto.displayName,
        fcmToken = dto.fcmToken,
        locationSharingEnabled = dto.locationSharingEnabled,
        createdAt = dto.createdAt,
    )

    fun toUser(dto: AuthUserDto): User = User(
        id = dto.id,
        email = dto.email,
        username = dto.username,
        displayName = dto.displayName,
        fcmToken = null,
        locationSharingEnabled = dto.locationSharingEnabled,
        createdAt = "",
    )

    fun toAuthSession(dto: AuthResponseDto): AuthSession = AuthSession(
        accessToken = dto.accessToken,
        refreshToken = dto.refreshToken,
        user = toUser(dto.user),
    )

    fun toEvent(dto: EventDto): Event = Event(
        id = dto.id,
        organizerId = dto.organizerId,
        title = dto.title,
        description = dto.description,
        latitude = dto.latitude,
        longitude = dto.longitude,
        address = dto.address,
        status = toEventStatus(dto.status),
        arrivalThresholdM = dto.arrivalThresholdM,
        startsAt = dto.startsAt,
        completedAt = dto.completedAt,
        createdAt = dto.createdAt,
    )

    fun toInvitation(dto: InvitationDto): Invitation = Invitation(
        id = dto.id,
        eventId = dto.eventId,
        inviteeId = dto.inviteeId,
        invitedById = dto.invitedById,
        status = toInvitationStatus(dto.status),
        createdAt = dto.createdAt,
    )

    fun toEventParticipant(dto: EventParticipantDto): EventParticipant = EventParticipant(
        id = dto.id,
        eventId = dto.eventId,
        userId = dto.userId,
        role = toParticipantRole(dto.role),
        sharingLocation = dto.sharingLocation,
        arrived = dto.arrived,
        lastLatitude = dto.lastLatitude,
        lastLongitude = dto.lastLongitude,
        lastLocationAt = dto.lastLocationAt,
        arrivedAt = dto.arrivedAt,
        createdAt = dto.createdAt,
    )

    fun toParticipantWithUser(dto: ParticipantWithUserDto): ParticipantWithUser =
        ParticipantWithUser(
            participant = toEventParticipant(
                EventParticipantDto(
                    id = dto.id,
                    eventId = dto.eventId,
                    userId = dto.userId,
                    role = dto.role,
                    sharingLocation = dto.sharingLocation,
                    arrived = dto.arrived,
                    lastLatitude = dto.lastLatitude,
                    lastLongitude = dto.lastLongitude,
                    lastLocationAt = dto.lastLocationAt,
                    arrivedAt = dto.arrivedAt,
                    createdAt = dto.createdAt,
                ),
            ),
            user = toUser(dto.user),
        )

    fun toLocationUpdateResult(dto: LocationUpdateResponseDto): LocationUpdateResult =
        LocationUpdateResult(
            participant = toEventParticipant(dto.participant),
            proximity = toProximityResult(dto.proximity),
        )

    fun toProximityResult(dto: ProximityResultDto): ProximityResult = ProximityResult(
        arrived = dto.arrived,
        eventCompleted = dto.eventCompleted,
    )

    fun toEventStatus(value: String): EventStatus = when (value.lowercase()) {
        "active" -> EventStatus.ACTIVE
        "completed" -> EventStatus.COMPLETED
        "cancelled" -> EventStatus.CANCELLED
        else -> EventStatus.SCHEDULED
    }

    fun toInvitationStatus(value: String): InvitationStatus = when (value.lowercase()) {
        "accepted" -> InvitationStatus.ACCEPTED
        "rejected" -> InvitationStatus.REJECTED
        else -> InvitationStatus.PENDING
    }

    fun toParticipantRole(value: String): ParticipantRole = when (value.lowercase()) {
        "organizer" -> ParticipantRole.ORGANIZER
        else -> ParticipantRole.PARTICIPANT
    }

    fun invitationStatusToApi(status: InvitationStatus): String = when (status) {
        InvitationStatus.ACCEPTED -> "accepted"
        InvitationStatus.REJECTED -> "rejected"
        InvitationStatus.PENDING -> "pending"
    }
}
