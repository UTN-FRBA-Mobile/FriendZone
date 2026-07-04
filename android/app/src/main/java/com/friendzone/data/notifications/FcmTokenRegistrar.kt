package com.example.friendzone.data.notifications

import android.util.Log
import com.example.friendzone.data.local.TokenManager
import com.example.friendzone.domain.repository.NotificationRepository
import com.example.friendzone.domain.result.ApiResult
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenRegistrar @Inject constructor(
    private val tokenManager: TokenManager,
    private val notificationRepository: NotificationRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun syncCurrentToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener(::uploadTokenIfLoggedIn)
            .addOnFailureListener { error ->
                Log.w(TAG, "Failed to get FCM token", error)
            }
    }

    fun uploadTokenIfLoggedIn(token: String) {
        scope.launch {
            if (tokenManager.getAccessToken().isNullOrBlank()) {
                return@launch
            }

            when (val result = notificationRepository.registerFcmToken(token)) {
                is ApiResult.Success -> Log.d(TAG, "FCM token registered")
                is ApiResult.Error -> Log.w(TAG, "Failed to register FCM token")
                ApiResult.Loading -> Unit
            }
        }
    }

    private companion object {
        const val TAG = "FcmTokenRegistrar"
    }
}
