package com.example.friendzone.data.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.friendzone.MainActivity
import com.example.friendzone.R
import com.example.friendzone.data.notifications.InboxSyncCoordinator
import com.example.friendzone.presentation.navigation.DeepLinkViewModel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.absoluteValue

@AndroidEntryPoint
class FriendZoneFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var fcmTokenRegistrar: FcmTokenRegistrar

    @Inject
    lateinit var inboxSyncCoordinator: InboxSyncCoordinator

    override fun onNewToken(token: String) {
        fcmTokenRegistrar.uploadTokenIfLoggedIn(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        inboxSyncCoordinator.invalidateInbox()
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        showNotification(title, body, message.data)
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationChannels.ensureDefaultChannel(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(DeepLinkViewModel.EXTRA_TYPE, data["type"])
            putExtra(EXTRA_NOTIFICATION_ID, data["notificationId"])
            putExtra(EXTRA_NOTIFICATION_TYPE, data["type"])
            putExtra(EXTRA_INVITATION_ID, data["invitationId"])
            putExtra(EXTRA_REQUEST_ID, data["requestId"])
            putExtra(EXTRA_EVENT_ID, data["eventId"])
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            data["notificationId"].hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, NotificationChannels.DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationId = data["notificationId"]?.hashCode()?.absoluteValue ?: title.hashCode().absoluteValue
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notificationId"
        const val EXTRA_NOTIFICATION_TYPE = "notificationType"
        const val EXTRA_INVITATION_ID = "invitationId"
        const val EXTRA_REQUEST_ID = "requestId"
        const val EXTRA_EVENT_ID = "eventId"
    }
}
