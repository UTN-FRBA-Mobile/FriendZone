package com.example.friendzone.presentation.navigation

object Screen {
    const val Login = "login"
    const val Register = "register"
    const val Events = "events"
    const val Friends = "friends"
    const val Profile = "profile"
    const val Create = "create"
    const val CreateStep1 = "create/step1"
    const val CreateStep2 = "create/step2"
    const val EventDetail = "events/detail/{eventId}?openMap={openMap}"
    const val Notifications = "notifications"

    fun eventDetail(eventId: String, openMap: Boolean = false) = "events/detail/$eventId?openMap=$openMap"

    val bottomBarRoutes = setOf(Events, Friends, Profile)
}
