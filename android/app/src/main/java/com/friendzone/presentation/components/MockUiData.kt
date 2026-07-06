package com.example.friendzone.presentation.components

data class FriendRowUi(
    val initial: String,
    val name: String,
    val subtitle: String,
    val pillText: String,
    val pillVariant: PillVariant,
    val profilePictureUrl: String? = null,
)

data class MockFriend(
    val emoji: String,
    val name: String,
    val subtitle: String,
    val pillText: String,
    val pillVariant: PillVariant,
) {
    fun toFriendRowUi(): FriendRowUi = FriendRowUi(
        initial = emoji,
        name = name,
        subtitle = subtitle,
        pillText = pillText,
        pillVariant = pillVariant,
    )
}

enum class PillVariant {
    Dark,
    Light,
    Green,
    Amber,
    Live,
}
