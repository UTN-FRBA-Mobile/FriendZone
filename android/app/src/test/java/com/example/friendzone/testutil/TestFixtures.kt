package com.example.friendzone.testutil

import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventParticipant
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.model.FriendRequest
import com.example.friendzone.domain.model.FriendRequestStatus
import com.example.friendzone.domain.model.Invitation
import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.domain.model.ParticipantRole
import com.example.friendzone.domain.model.ParticipantWithUser
import com.example.friendzone.domain.model.PendingInvitation
import com.example.friendzone.domain.model.User
import java.time.Instant

private val fixtureNow = Instant.parse("2026-07-05T18:00:00Z")

fun testUser(
    id: String = "user-1",
    email: String = "user@example.com",
    username: String = "testuser",
    displayName: String = "Test User",
    profilePictureUrl: String? = null,
): User = User(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    fcmToken = null,
    locationSharingEnabled = true,
    profilePictureUrl = profilePictureUrl,
    createdAt = fixtureNow.toString(),
)

fun testEvent(
    id: String = "event-1",
    organizerId: String = "org-1",
    title: String = "Test Event",
    status: EventStatus = EventStatus.SCHEDULED,
    startsAt: String = fixtureNow.plusSeconds(3600).toString(),
    trackingLeadMinutes: Int = 30,
): Event = Event(
    id = id,
    organizerId = organizerId,
    title = title,
    description = null,
    latitude = 40.7128,
    longitude = -74.0060,
    address = null,
    status = status,
    arrivalThresholdM = 100,
    trackingLeadMinutes = trackingLeadMinutes,
    startsAt = startsAt,
    completedAt = null,
    createdAt = fixtureNow.toString(),
)

fun testParticipant(
    id: String = "participant-1",
    eventId: String = "event-1",
    userId: String = "user-1",
    sharingLocation: Boolean = false,
    arrived: Boolean = false,
): EventParticipant = EventParticipant(
    id = id,
    eventId = eventId,
    userId = userId,
    role = ParticipantRole.PARTICIPANT,
    sharingLocation = sharingLocation,
    arrived = arrived,
    lastLatitude = null,
    lastLongitude = null,
    lastLocationAt = null,
    arrivedAt = null,
    createdAt = fixtureNow.toString(),
)

fun testParticipantWithUser(
    participant: EventParticipant = testParticipant(),
    user: User = testUser(id = participant.userId),
): ParticipantWithUser = ParticipantWithUser(participant = participant, user = user)

fun testInvitation(
    id: String = "inv-1",
    eventId: String = "event-1",
    status: InvitationStatus = InvitationStatus.PENDING,
): Invitation = Invitation(
    id = id,
    eventId = eventId,
    inviteeId = "user-2",
    invitedById = "org-1",
    status = status,
    createdAt = fixtureNow.toString(),
)

fun testPendingInvitation(
    id: String = "pending-inv-1",
    eventId: String = "event-1",
    eventTitle: String = "Party",
): PendingInvitation = PendingInvitation(
    id = id,
    eventId = eventId,
    inviteeId = "user-1",
    invitedById = "org-1",
    status = InvitationStatus.PENDING,
    createdAt = fixtureNow.toString(),
    eventTitle = eventTitle,
    eventStartsAt = fixtureNow.plusSeconds(3600).toString(),
    organizerDisplayName = "Organizer",
)

fun testFriendRequest(
    id: String = "req-1",
    requester: User = testUser(id = "user-2", username = "friend"),
): FriendRequest = FriendRequest(
    id = id,
    requesterId = requester.id,
    addresseeId = "user-1",
    status = FriendRequestStatus.PENDING,
    createdAt = fixtureNow.toString(),
    respondedAt = null,
    requester = requester,
)
