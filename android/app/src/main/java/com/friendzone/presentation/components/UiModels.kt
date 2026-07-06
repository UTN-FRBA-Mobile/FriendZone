package com.example.friendzone.presentation.components

data class FriendRowUi(
    val initial: String,
    val name: String,
    val subtitle: String,
    val pillText: String,
    val pillVariant: PillVariant,
    val profilePictureUrl: String? = null,
)

enum class PillVariant {
    Dark,
    Light,
    Green,
    Amber,
    Live,
}
