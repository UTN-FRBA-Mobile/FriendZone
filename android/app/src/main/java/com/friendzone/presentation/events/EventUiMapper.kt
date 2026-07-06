package com.example.friendzone.presentation.events

import android.content.Context
import com.example.friendzone.R
import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.domain.model.ParticipantWithUser
import com.example.friendzone.domain.util.ParticipantStatus
import com.example.friendzone.domain.util.RelativeTimeLabel
import com.example.friendzone.domain.util.classifyParticipantWithUser
import com.example.friendzone.domain.util.formatEventDate
import com.example.friendzone.domain.util.getRelativeTimeLabel
import com.example.friendzone.domain.util.isLive
import com.example.friendzone.domain.util.resolveApiAssetUrl
import com.example.friendzone.presentation.components.FriendRowUi
import com.example.friendzone.presentation.components.PillVariant

fun Event.toListItemUi(
    context: Context,
    confirmedCount: Int,
    pendingCount: Int,
    onTheWayCount: Int = 0,
    friendPreviews: List<FriendRowUi> = emptyList(),
    participantAvatars: List<UserAvatarUi> = emptyList(),
    extraAvatarCount: Int = 0,
    isPastItem: Boolean = false,
    startsAtEpoch: Long = 0L,
    organizerId: String? = null,
    currentUserId: String? = null,
): EventListItemUi {
    val (icon, relativeTime) = getRelativeTimeLabel(startsAt)
    val timeLabel = when (relativeTime) {
        RelativeTimeLabel.Today -> context.getString(R.string.msg_today)
        RelativeTimeLabel.Tomorrow -> context.getString(R.string.msg_tomorrow)
        RelativeTimeLabel.Yesterday -> context.getString(R.string.msg_yesterday)
        is RelativeTimeLabel.InDays -> context.getString(R.string.msg_in_days, relativeTime.days.toInt())
        is RelativeTimeLabel.DaysAgo -> context.getString(R.string.msg_days_ago, relativeTime.days.toInt())
        RelativeTimeLabel.NextWeek -> context.getString(R.string.msg_next_week)
    }

    val statusBadge = when (status) {
        EventStatus.COMPLETED -> EventDetailStatusBadge.Completed
        EventStatus.CANCELLED -> EventDetailStatusBadge.Cancelled
        else -> null
    }
    return EventListItemUi(
        eventId = id,
        title = title,
        timeIcon = icon,
        timeLabel = timeLabel,
        dateText = formatEventDate(startsAt),
        statusBadge = statusBadge,
        confirmedText = context.getString(R.string.msg_confirmed_count, confirmedCount),
        pendingText = context.getString(R.string.msg_pending_count, pendingCount),
        onTheWayText = if (onTheWayCount > 0) context.getString(R.string.msg_on_the_way_count, onTheWayCount) else null,
        isLive = isLive(),
        avatars = participantAvatars,
        extraCount = extraAvatarCount,
        friendPreviews = friendPreviews,
        isOrganizer = currentUserId != null && organizerId == currentUserId,
        organizerId = organizerId,
        currentUserId = currentUserId,
        coverImageUrl = resolveApiAssetUrl(coverImageUrl),
        isPastItem = isPastItem,
        startsAtEpoch = startsAtEpoch,
    )
}

fun buildFriendPreviews(
    context: Context,
    event: Event,
    participants: List<ParticipantWithUser>,
    limit: Int = 3,
): List<FriendRowUi> =
    participants
        .filter { it.participant.sharingLocation || it.participant.arrived }
        .take(limit)
        .map { item ->
            friendRowForParticipantStatus(
                context = context,
                displayName = item.user.displayName,
                profilePictureUrl = resolveApiAssetUrl(item.user.profilePictureUrl),
                status = classifyParticipantWithUser(item, event),
            )
        }

fun friendRowForParticipantStatus(
    context: Context,
    displayName: String,
    status: ParticipantStatus,
    profilePictureUrl: String? = null,
): FriendRowUi = when (status) {
    is ParticipantStatus.Arrived -> participantToFriendRow(
        displayName = displayName,
        subtitle = context.getString(R.string.msg_already_there),
        pillText = context.getString(R.string.msg_status_arrived_badge),
        pillVariant = PillVariant.Dark,
        profilePictureUrl = profilePictureUrl,
    )
    is ParticipantStatus.InTransit -> participantToFriendRow(
        displayName = displayName,
        subtitle = status.etaMinutes?.let { context.getString(R.string.msg_min_away, it) } ?: context.getString(R.string.msg_arrival_unavailable),
        pillText = context.getString(R.string.msg_status_in_transit_badge),
        pillVariant = PillVariant.Light,
        profilePictureUrl = profilePictureUrl,
    )
    is ParticipantStatus.Delayed -> participantToFriendRow(
        displayName = displayName,
        subtitle = status.etaMinutes?.let { context.getString(R.string.msg_min_away, it) } ?: context.getString(R.string.msg_arrival_unavailable),
        pillText = context.getString(R.string.msg_status_delayed_badge),
        pillVariant = PillVariant.Amber,
        profilePictureUrl = profilePictureUrl,
    )
}

fun countInvitations(
    invitations: List<com.example.friendzone.domain.model.Invitation>,
): Pair<Int, Int> {
    val confirmed = invitations.count { it.status == InvitationStatus.ACCEPTED }
    val pending = invitations.count { it.status == InvitationStatus.PENDING }
    return confirmed to pending
}

fun buildAvatarPreview(participants: List<ParticipantWithUser>): Pair<List<UserAvatarUi>, Int> {
    val avatars = participants.map { item ->
        UserAvatarUi(
            displayName = item.user.displayName,
            profilePictureUrl = resolveApiAssetUrl(item.user.profilePictureUrl),
        )
    }
    val shown = avatars.take(4)
    val extra = (avatars.size - shown.size).coerceAtLeast(0)
    return shown to extra
}
