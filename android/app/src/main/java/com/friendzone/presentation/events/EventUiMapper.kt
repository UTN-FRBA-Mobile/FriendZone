package com.example.friendzone.presentation.events

import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.domain.model.ParticipantWithUser
import com.example.friendzone.domain.util.ParticipantStatus
import com.example.friendzone.domain.util.classifyParticipantWithUser
import com.example.friendzone.domain.util.formatEventDate
import com.example.friendzone.domain.util.formatRelativeTimeLabel
import com.example.friendzone.domain.util.isLive
import com.example.friendzone.domain.util.resolveApiAssetUrl
import com.example.friendzone.domain.util.statusPillText
import com.example.friendzone.domain.util.travelEtaSubtitle
import com.example.friendzone.presentation.components.FriendRowUi
import com.example.friendzone.presentation.components.PillVariant

fun Event.toListItemUi(
    confirmedCount: Int,
    pendingCount: Int,
    onTheWayCount: Int = 0,
    friendPreviews: List<com.example.friendzone.presentation.components.FriendRowUi> = emptyList(),
    participantAvatars: List<String> = emptyList(),
    extraAvatarCount: Int = 0,
    isPastItem: Boolean = false,
    startsAtEpoch: Long = 0L,
): EventListItemUi {
    val (icon, label) = formatRelativeTimeLabel(startsAt)
    val statusBadge = when (status) {
        EventStatus.COMPLETED -> EventDetailStatusBadge.Completed
        EventStatus.CANCELLED -> EventDetailStatusBadge.Cancelled
        else -> null
    }
    return EventListItemUi(
        eventId = id,
        title = title,
        timeIcon = icon,
        timeLabel = label,
        dateText = formatEventDate(startsAt),
        statusBadge = statusBadge,
        confirmedText = "✓ $confirmedCount Confirmed",
        pendingText = "? $pendingCount Pending",
        onTheWayText = if (onTheWayCount > 0) "🚗 $onTheWayCount On the way" else null,
        isLive = isLive(),
        avatars = participantAvatars,
        extraCount = extraAvatarCount,
        friendPreviews = friendPreviews,
        coverImageUrl = resolveApiAssetUrl(coverImageUrl),
        isPastItem = isPastItem,
        startsAtEpoch = startsAtEpoch,
    )
}

fun buildFriendPreviews(
    event: Event,
    participants: List<ParticipantWithUser>,
    limit: Int = 3,
): List<FriendRowUi> =
    participants
        .filter { it.participant.sharingLocation || it.participant.arrived }
        .take(limit)
        .map { item ->
            friendRowForParticipantStatus(
                displayName = item.user.displayName,
                status = classifyParticipantWithUser(item, event),
                arrivedSubtitle = "Already there",
            )
        }

fun friendRowForParticipantStatus(
    displayName: String,
    status: ParticipantStatus,
    arrivedSubtitle: String = "Arrived",
): FriendRowUi = when (status) {
    is ParticipantStatus.Arrived -> participantToFriendRow(
        displayName = displayName,
        subtitle = arrivedSubtitle,
        pillText = status.statusPillText(),
        pillVariant = PillVariant.Dark,
    )
    is ParticipantStatus.InTransit -> participantToFriendRow(
        displayName = displayName,
        subtitle = status.travelEtaSubtitle(),
        pillText = status.statusPillText(),
        pillVariant = PillVariant.Light,
    )
    is ParticipantStatus.Delayed -> participantToFriendRow(
        displayName = displayName,
        subtitle = status.travelEtaSubtitle(),
        pillText = status.statusPillText(),
        pillVariant = PillVariant.Amber,
    )
}

fun countInvitations(
    invitations: List<com.example.friendzone.domain.model.Invitation>,
): Pair<Int, Int> {
    val confirmed = invitations.count { it.status == InvitationStatus.ACCEPTED }
    val pending = invitations.count { it.status == InvitationStatus.PENDING }
    return confirmed to pending
}

fun buildAvatarPreview(participants: List<ParticipantWithUser>): Pair<List<String>, Int> {
    val initials = participants.map {
        it.user.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }
    val shown = initials.take(4)
    val extra = (initials.size - shown.size).coerceAtLeast(0)
    return shown to extra
}
