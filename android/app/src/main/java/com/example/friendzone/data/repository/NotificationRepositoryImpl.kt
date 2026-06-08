package com.example.friendzone.data.repository

import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.NotificationsInboxApi
import com.example.friendzone.data.remote.api.UsersApi
import com.example.friendzone.data.remote.dto.UpdateFcmTokenRequest
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.InboxNotification
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.NotificationRepository
import com.example.friendzone.domain.result.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi,
    private val notificationsInboxApi: NotificationsInboxApi,
) : NotificationRepository {
    override suspend fun registerFcmToken(token: String): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(usersApi.updateFcmToken(UpdateFcmTokenRequest(token)))
    }

    override suspend fun getInbox(): ApiResult<List<InboxNotification>> = safeApiCall {
        notificationsInboxApi.getInbox().map(DtoMapper::toInboxNotification)
    }

    override suspend fun getBadgeCount(): ApiResult<Int> = safeApiCall {
        notificationsInboxApi.getBadgeCount().count
    }

    override suspend fun markRead(notificationId: String): ApiResult<InboxNotification> =
        safeApiCall {
            DtoMapper.toInboxNotification(notificationsInboxApi.markRead(notificationId))
        }
}
