package com.example.friendzone.presentation.events

import com.example.friendzone.presentation.components.FriendRowUi
import com.example.friendzone.presentation.components.PillVariant

data class EventListItemUi(
    val eventId: String,
    val title: String,
    val timeIcon: String,
    val timeLabel: String,
    val dateText: String,
    val confirmedText: String,
    val pendingText: String,
    val onTheWayText: String? = null,
    val isLive: Boolean,
    val avatars: List<String> = emptyList(),
    val extraCount: Int = 0,
    val friendPreviews: List<FriendRowUi> = emptyList(),
)

fun participantToFriendRow(
    displayName: String,
    subtitle: String,
    pillText: String,
    pillVariant: PillVariant,
): FriendRowUi = FriendRowUi(
    initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
    name = displayName,
    subtitle = subtitle,
    pillText = pillText,
    pillVariant = pillVariant,
)
