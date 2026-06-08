package com.example.friendzone.data.remote.api

import com.example.friendzone.data.remote.dto.InboxNotificationDto
import com.example.friendzone.data.remote.dto.NotificationBadgeCountDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationsInboxApi {
    @GET("notifications/inbox")
    suspend fun getInbox(): List<InboxNotificationDto>

    @GET("notifications/badge-count")
    suspend fun getBadgeCount(): NotificationBadgeCountDto

    @PATCH("notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): InboxNotificationDto
}
