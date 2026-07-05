package com.example.friendzone.presentation.navigation

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.friendzone.data.notifications.FriendZoneFirebaseMessagingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DeepLink(
    val type: String?,
    val invitationId: String? = null,
    val requestId: String? = null,
    val eventId: String? = null,
    val inviteUsername: String? = null,
)

@HiltViewModel
class DeepLinkViewModel @Inject constructor() : ViewModel() {
    private val _pending = MutableStateFlow<DeepLink?>(null)
    val pending: StateFlow<DeepLink?> = _pending.asStateFlow()

    fun consumeFromIntent(intent: Intent?) {
        if (intent == null) return

        // App Link: https://<host>/invite/<username>
        if (intent.action == Intent.ACTION_VIEW) {
            val segments = intent.data?.pathSegments.orEmpty()
            if (segments.size >= 2 && segments[0] == "invite") {
                val username = segments[1].takeIf { it.isNotBlank() }
                if (username != null) {
                    _pending.value = DeepLink(type = "invite.link", inviteUsername = username)
                }
                intent.data = null
                return
            }
        }

        val type = intent.getStringExtra(FriendZoneFirebaseMessagingService.EXTRA_NOTIFICATION_TYPE)
        val invitationId = intent.getStringExtra(FriendZoneFirebaseMessagingService.EXTRA_INVITATION_ID)
        val requestId = intent.getStringExtra(FriendZoneFirebaseMessagingService.EXTRA_REQUEST_ID)
        val eventId = intent.getStringExtra(FriendZoneFirebaseMessagingService.EXTRA_EVENT_ID)
        if (type != null || invitationId != null || requestId != null) {
            _pending.value = DeepLink(
                type = type,
                invitationId = invitationId,
                requestId = requestId,
                eventId = eventId,
            )
        }
        intent.removeExtra(FriendZoneFirebaseMessagingService.EXTRA_NOTIFICATION_TYPE)
        intent.removeExtra(FriendZoneFirebaseMessagingService.EXTRA_INVITATION_ID)
        intent.removeExtra(FriendZoneFirebaseMessagingService.EXTRA_REQUEST_ID)
        intent.removeExtra(FriendZoneFirebaseMessagingService.EXTRA_EVENT_ID)
        intent.removeExtra(FriendZoneFirebaseMessagingService.EXTRA_NOTIFICATION_ID)
    }

    fun clear() {
        _pending.value = null
    }
}
