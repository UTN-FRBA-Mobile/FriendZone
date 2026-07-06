package com.example.friendzone.presentation.events

import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.testutil.testEvent
import com.example.friendzone.testutil.testInvitation
import com.example.friendzone.testutil.testParticipant
import com.example.friendzone.testutil.testParticipantWithUser
import com.example.friendzone.testutil.testUser
import org.junit.Assert.assertEquals
import org.junit.Test

class EventUiMapperTest {
    @Test
    fun countInvitations_mixedStatuses_returnsConfirmedAndPendingCounts() {
        val invitations = listOf(
            testInvitation(id = "1", status = InvitationStatus.ACCEPTED),
            testInvitation(id = "2", status = InvitationStatus.ACCEPTED),
            testInvitation(id = "3", status = InvitationStatus.PENDING),
            testInvitation(id = "4", status = InvitationStatus.REJECTED),
        )

        val (confirmed, pending) = countInvitations(invitations)

        assertEquals(2, confirmed)
        assertEquals(1, pending)
    }

    @Test
    fun buildAvatarPreview_noParticipants_returnsEmptyShownAndZeroExtra() {
        val (shown, extra) = buildAvatarPreview(emptyList())

        assertEquals(emptyList<String>(), shown)
        assertEquals(0, extra)
    }

    @Test
    fun buildAvatarPreview_threeParticipants_returnsAllShownAndZeroExtra() {
        val participants = (1..3).map { index ->
            testParticipantWithUser(user = testUser(id = "u$index", displayName = "User $index"))
        }

        val (shown, extra) = buildAvatarPreview(participants)

        assertEquals(listOf("U", "U", "U"), shown)
        assertEquals(0, extra)
    }

    @Test
    fun buildAvatarPreview_sixParticipants_returnsFourShownAndTwoExtra() {
        val participants = (1..6).map { index ->
            testParticipantWithUser(user = testUser(id = "u$index", displayName = "User $index"))
        }

        val (shown, extra) = buildAvatarPreview(participants)

        assertEquals(4, shown.size)
        assertEquals(2, extra)
    }

    @Test
    fun buildFriendPreviews_filtersParticipantsNotSharingOrArrived() {
        val event = testEvent()
        val sharing = testParticipantWithUser(
            participant = testParticipant(id = "p1", sharingLocation = true),
            user = testUser(id = "u1", displayName = "Sharing"),
        )
        val arrived = testParticipantWithUser(
            participant = testParticipant(id = "p2", arrived = true),
            user = testUser(id = "u2", displayName = "Arrived"),
        )
        val hidden = testParticipantWithUser(
            participant = testParticipant(id = "p3", sharingLocation = false, arrived = false),
            user = testUser(id = "u3", displayName = "Hidden"),
        )

        val previews = buildFriendPreviews(event, listOf(sharing, arrived, hidden))

        assertEquals(2, previews.size)
        assertEquals("Sharing", previews[0].name)
        assertEquals("Arrived", previews[1].name)
    }
}
