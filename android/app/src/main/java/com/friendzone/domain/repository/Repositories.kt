package com.example.friendzone.domain.repository

import com.example.friendzone.domain.model.AuthSession
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.result.ApiResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUser: Flow<User?>
    suspend fun register(
        email: String,
        username: String,
        password: String,
        displayName: String,
    ): ApiResult<AuthSession>
    suspend fun login(emailOrUsername: String, password: String): ApiResult<AuthSession>
    suspend fun logout(): ApiResult<Unit>
}

interface UserRepository {
    suspend fun getMe(): ApiResult<User>
    suspend fun getFriends(): ApiResult<List<User>>
    suspend fun updateProfile(displayName: String): ApiResult<User>
    suspend fun updateLocationSharing(enabled: Boolean): ApiResult<User>
    suspend fun updateFcmToken(token: String): ApiResult<User>
    suspend fun search(query: String): ApiResult<List<User>>
    suspend fun lookup(query: String): ApiResult<User>
}

interface FriendRepository {
    suspend fun getFriends(): ApiResult<List<User>>
    suspend fun getIncomingRequests(): ApiResult<List<com.example.friendzone.domain.model.FriendRequest>>
    suspend fun getPendingIncomingCount(): ApiResult<Int>
    suspend fun sendRequest(emailOrUsername: String): ApiResult<com.example.friendzone.domain.model.FriendRequest>
    suspend fun respondToRequest(
        requestId: String,
        status: com.example.friendzone.domain.model.FriendRequestStatus,
    ): ApiResult<Unit>
}

interface EventRepository {
    suspend fun create(
        title: String,
        description: String?,
        latitude: Double,
        longitude: Double,
        address: String?,
        startsAt: String,
        arrivalThresholdM: Int? = null,
        trackingLeadMinutes: Int = 30,
    ): ApiResult<com.example.friendzone.domain.model.Event>
    suspend fun getMine(): ApiResult<List<com.example.friendzone.domain.model.Event>>
    suspend fun getById(id: String): ApiResult<com.example.friendzone.domain.model.Event>
    suspend fun update(
        id: String,
        title: String?,
        description: String?,
        latitude: Double?,
        longitude: Double?,
        address: String?,
        startsAt: String?,
        arrivalThresholdM: Int?,
    ): ApiResult<com.example.friendzone.domain.model.Event>
    suspend fun updateStatus(
        id: String,
        status: com.example.friendzone.domain.model.EventStatus,
    ): ApiResult<com.example.friendzone.domain.model.Event>
    suspend fun uploadCover(id: String, bytes: ByteArray, mimeType: String): ApiResult<com.example.friendzone.domain.model.Event>
    suspend fun delete(id: String): ApiResult<Unit>
}

interface InvitationRepository {
    suspend fun create(eventId: String, emailOrUsername: String): ApiResult<com.example.friendzone.domain.model.Invitation>
    suspend fun getByEvent(eventId: String): ApiResult<List<com.example.friendzone.domain.model.Invitation>>
    suspend fun getMinePending(): ApiResult<List<com.example.friendzone.domain.model.PendingInvitation>>
    suspend fun respond(
        invitationId: String,
        status: com.example.friendzone.domain.model.InvitationStatus,
    ): ApiResult<com.example.friendzone.domain.model.Invitation>
}

interface LocationRepository {
    suspend fun updateSharing(eventId: String, enabled: Boolean): ApiResult<com.example.friendzone.domain.model.EventParticipant>
    suspend fun updateLocation(
        eventId: String,
        latitude: Double,
        longitude: Double,
    ): ApiResult<com.example.friendzone.domain.model.LocationUpdateResult>
    suspend fun getParticipants(eventId: String): ApiResult<List<com.example.friendzone.domain.model.ParticipantWithUser>>
}

interface NotificationRepository {
    suspend fun registerFcmToken(token: String): ApiResult<User>
    suspend fun getInbox(): ApiResult<List<com.example.friendzone.domain.model.InboxNotification>>
    suspend fun getBadgeCount(): ApiResult<Int>
    suspend fun markRead(notificationId: String): ApiResult<com.example.friendzone.domain.model.InboxNotification>
}
