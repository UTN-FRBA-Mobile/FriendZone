package com.example.friendzone.presentation.navigation

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.friendzone.data.notifications.FriendZoneFirebaseMessagingService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkViewModelTest {
    @Test
    fun consumeFromIntent_inviteAppLink_setsInviteDeepLink() {
        val viewModel = DeepLinkViewModel()
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://friendzone.app/invite/johndoe"),
        )

        viewModel.consumeFromIntent(intent)

        assertEquals(
            DeepLink(type = "invite.link", inviteUsername = "johndoe"),
            viewModel.pending.value,
        )
    }

    @Test
    fun consumeFromIntent_fcmExtras_setsNotificationDeepLink() {
        val viewModel = DeepLinkViewModel()
        val intent = Intent().apply {
            putExtra(FriendZoneFirebaseMessagingService.EXTRA_NOTIFICATION_TYPE, "invitation.created")
            putExtra(FriendZoneFirebaseMessagingService.EXTRA_INVITATION_ID, "inv-42")
            putExtra(FriendZoneFirebaseMessagingService.EXTRA_REQUEST_ID, "req-7")
            putExtra(FriendZoneFirebaseMessagingService.EXTRA_EVENT_ID, "event-9")
        }

        viewModel.consumeFromIntent(intent)

        assertEquals(
            DeepLink(
                type = "invitation.created",
                invitationId = "inv-42",
                requestId = "req-7",
                eventId = "event-9",
            ),
            viewModel.pending.value,
        )
    }

    @Test
    fun consumeFromIntent_nullIntent_leavesPendingNull() {
        val viewModel = DeepLinkViewModel()

        viewModel.consumeFromIntent(null)

        assertNull(viewModel.pending.value)
    }

    @Test
    fun consumeFromIntent_emptyIntent_leavesPendingNull() {
        val viewModel = DeepLinkViewModel()

        viewModel.consumeFromIntent(Intent())

        assertNull(viewModel.pending.value)
    }
}
