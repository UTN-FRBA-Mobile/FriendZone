package com.example.friendzone.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.domain.model.AppNotificationType
import com.example.friendzone.domain.model.FriendRequestStatus
import com.example.friendzone.domain.model.InboxNotification
import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.repository.InvitationRepository
import com.example.friendzone.domain.repository.NotificationRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import com.example.friendzone.domain.util.formatEventDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val items: List<InboxNotification> = emptyList(),
    val errorMessage: String? = null,
    val selectedNotification: InboxNotification? = null,
    val isActionLoading: Boolean = false,
    val snackbarMessage: String? = null,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val friendRepository: FriendRepository,
    private val invitationRepository: InvitationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _actionFinished = MutableStateFlow(false)
    val actionFinished: StateFlow<Boolean> = _actionFinished.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadInbox() {
        viewModelScope.launch {
            loadInboxInternal(showFullLoading = true)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            try {
                loadInboxInternal(showFullLoading = false)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun loadInboxInternal(showFullLoading: Boolean) {
        if (showFullLoading) _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        when (val result = notificationRepository.getInbox()) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(isLoading = false, items = result.data, errorMessage = null)
                }
            }
            is ApiResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.error.displayMessage(),
                    )
                }
            }
            ApiResult.Loading -> Unit
        }
    }

    fun selectNotification(notification: InboxNotification) {
        if (notification.actionable) {
            _uiState.update { it.copy(selectedNotification = notification) }
        } else {
            markRead(notification.id)
        }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(selectedNotification = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun resetActionFinished() {
        _actionFinished.value = false
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            when (val result = notificationRepository.markRead(notificationId)) {
                is ApiResult.Success -> loadInbox()
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(snackbarMessage = result.error.displayMessage())
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun acceptSelected() {
        val notification = _uiState.value.selectedNotification ?: return
        respondToSelected(accept = true, notification)
    }

    fun rejectSelected() {
        val notification = _uiState.value.selectedNotification ?: return
        respondToSelected(accept = false, notification)
    }

    private fun respondToSelected(accept: Boolean, notification: InboxNotification) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            val result = when (notification.type) {
                AppNotificationType.FRIEND_REQUEST -> {
                    val requestId = notification.data["requestId"] ?: return@launch
                    friendRepository.respondToRequest(
                        requestId,
                        if (accept) FriendRequestStatus.ACCEPTED else FriendRequestStatus.REJECTED,
                    )
                }
                AppNotificationType.INVITATION_CREATED -> {
                    val invitationId = notification.data["invitationId"] ?: return@launch
                    invitationRepository.respond(
                        invitationId,
                        if (accept) InvitationStatus.ACCEPTED else InvitationStatus.REJECTED,
                    )
                }
                else -> return@launch
            }

            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            selectedNotification = null,
                            snackbarMessage = when {
                                accept && notification.type == AppNotificationType.INVITATION_CREATED ->
                                    "Joined event"
                                accept -> "Accepted"
                                else -> "Declined"
                            },
                        )
                    }
                    _actionFinished.value = true
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            snackbarMessage = result.error.displayMessage(),
                        )
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun detailSubtitle(notification: InboxNotification): String? = when (notification.type) {
        AppNotificationType.INVITATION_CREATED -> {
            val organizer = notification.data["organizerDisplayName"]
            val startsAt = notification.data["eventStartsAt"]?.let(::formatEventDate)
            listOfNotNull(organizer?.let { "From $it" }, startsAt).joinToString(" · ")
                .ifBlank { null }
        }
        AppNotificationType.FRIEND_REQUEST -> {
            notification.data["requesterDisplayName"]?.let { "From $it" }
        }
        else -> notification.data["eventTitle"]
    }
}
