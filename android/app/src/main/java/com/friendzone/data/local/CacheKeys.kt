package com.example.friendzone.data.local

object CacheKeys {
    const val EVENTS_MINE = "events_mine"
    const val FRIENDS = "friends"
    const val FRIEND_REQUESTS = "friend_requests"
    const val NOTIFICATIONS_INBOX = "notifications_inbox"
    const val PENDING_INVITATIONS = "pending_invitations"
    const val EVENT_INVITATIONS_PREFIX = "event_invitations_"
    const val EVENT_PARTICIPANTS_PREFIX = "event_participants_"

    fun eventInvitations(eventId: String) = "$EVENT_INVITATIONS_PREFIX$eventId"

    fun eventParticipants(eventId: String) = "$EVENT_PARTICIPANTS_PREFIX$eventId"
}
