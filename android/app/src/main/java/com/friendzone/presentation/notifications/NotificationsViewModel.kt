package com.example.friendzone.presentation.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.R
import com.example.friendzone.data.notifications.InboxSyncCoordinator
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    inboxSyncCoordinator: InboxSyncCoordinator,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _actionFinished = MutableStateFlow(false)
    val actionFinished: StateFlow<Boolean> = _actionFinished.asStateFlow()

    private val _badgeRefreshNeeded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val badgeRefreshNeeded: SharedFlow<Unit> = _badgeRefreshNeeded.asSharedFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            inboxSyncCoordinator.invalidations.collect {
                refresh()
            }
        }
    }

    fun loadInbox() {
        viewModelScope.launch {
            loadInboxInternal(showFullLoading = _uiState.value.items.isEmpty())
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
        val cached = notificationRepository.getCachedInbox()
        if (cached != null) {
            _uiState.update {
                it.copy(isLoading = false, items = cached, errorMessage = null)
            }
        } else if (showFullLoading) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        }

        when (val result = notificationRepository.getInbox()) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(isLoading = false, items = result.data, errorMessage = null)
                }
            }
            is ApiResult.Error -> {
                if (cached == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error.displayMessage(context),
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
            ApiResult.Loading -> Unit
        }
    }

    fun selectNotification(notification: InboxNotification) {
        if (notification.actionable) {
            _uiState.update { it.copy(selectedNotification = notification) }
        } else {
            dismissNotification(notification)
        }
    }

    fun dismissNotification(notification: InboxNotification) {
        val previousItems = _uiState.value.items
        if (previousItems.none { it.id == notification.id }) return

        _uiState.update { state ->
            state.copy(items = state.items.filter { it.id != notification.id })
        }

        viewModelScope.launch {
            notificationRepository.removeFromCache(notification.id)
            when (val result = notificationRepository.markRead(notification.id)) {
                is ApiResult.Success -> {
                    _badgeRefreshNeeded.tryEmit(Unit)
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            items = previousItems,
                            snackbarMessage = result.error.displayMessage(context),
                        )
                    }
                }
                ApiResult.Loading -> Unit
            }
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
                    _uiState.update { state ->
                        state.copy(
                            isActionLoading = false,
                            selectedNotification = null,
                            items = state.items.filter { it.id != notification.id },
                            snackbarMessage = when {
                                accept && notification.type == AppNotificationType.INVITATION_CREATED ->
                                    context.getString(R.string.msg_joined_event)
                                accept -> context.getString(R.string.btn_accept)
                                else -> context.getString(R.string.msg_declined)
                            },
                        )
                    }
                    notificationRepository.removeFromCache(notification.id)
                    _badgeRefreshNeeded.tryEmit(Unit)
                    _actionFinished.value = true
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            snackbarMessage = result.error.displayMessage(context),
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
            val fromLabel = context.getString(R.string.msg_invitation_from, organizer ?: "", startsAt ?: "")
            fromLabel.ifBlank { null }
        }
        AppNotificationType.FRIEND_REQUEST -> {
            notification.data["requesterDisplayName"]?.let { context.getString(R.string.msg_invitation_from, it, "") } // Simplified
        }
        else -> notification.data["eventTitle"]
    }
}
