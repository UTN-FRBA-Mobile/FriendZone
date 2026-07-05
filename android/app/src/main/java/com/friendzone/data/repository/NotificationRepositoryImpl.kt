package com.example.friendzone.data.repository

import com.example.friendzone.data.local.CacheKeys
import com.example.friendzone.data.local.LocalCacheManager
import com.example.friendzone.data.mapper.DtoMapper
import com.example.friendzone.data.remote.api.NotificationsInboxApi
import com.example.friendzone.data.remote.api.UsersApi
import com.example.friendzone.data.remote.dto.UpdateFcmTokenRequest
import com.example.friendzone.data.remote.safeApiCall
import com.example.friendzone.domain.model.InboxNotification
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.NotificationRepository
import com.example.friendzone.domain.result.ApiResult
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi,
    private val notificationsInboxApi: NotificationsInboxApi,
    private val cacheManager: LocalCacheManager,
) : NotificationRepository {
    override suspend fun getCachedInbox(): List<InboxNotification>? =
        cacheManager.getList(CacheKeys.NOTIFICATIONS_INBOX, serializer())

    override suspend fun removeFromCache(notificationId: String) {
        val cached = getCachedInbox() ?: return
        cacheManager.putList(
            CacheKeys.NOTIFICATIONS_INBOX,
            cached.filter { it.id != notificationId },
            serializer(),
        )
    }

    override suspend fun registerFcmToken(token: String): ApiResult<User> = safeApiCall {
        DtoMapper.toUser(usersApi.updateFcmToken(UpdateFcmTokenRequest(token)))
    }

    override suspend fun getInbox(): ApiResult<List<InboxNotification>> {
        val result = safeApiCall {
            notificationsInboxApi.getInbox().map(DtoMapper::toInboxNotification)
        }
        return when (result) {
            is ApiResult.Success -> {
                cacheManager.putList(CacheKeys.NOTIFICATIONS_INBOX, result.data, serializer())
                result
            }
            is ApiResult.Error -> getCachedInbox()?.let { ApiResult.Success(it) } ?: result
            ApiResult.Loading -> result
        }
    }

    override suspend fun getBadgeCount(): ApiResult<Int> = safeApiCall {
        notificationsInboxApi.getBadgeCount().count
    }

    override suspend fun markRead(notificationId: String): ApiResult<InboxNotification> =
        safeApiCall {
            DtoMapper.toInboxNotification(notificationsInboxApi.markRead(notificationId))
        }.also { result ->
            if (result is ApiResult.Success) {
                removeFromCache(notificationId)
            }
        }
}
