package com.example.friendzone.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.data.notifications.InboxSyncCoordinator
import com.example.friendzone.domain.repository.NotificationRepository
import com.example.friendzone.domain.result.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsBadgeViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    inboxSyncCoordinator: InboxSyncCoordinator,
) : ViewModel() {
    private val _badgeCount = MutableStateFlow(0)
    val badgeCount: StateFlow<Int> = _badgeCount.asStateFlow()

    init {
        viewModelScope.launch {
            inboxSyncCoordinator.invalidations.collect {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = notificationRepository.getBadgeCount()) {
                is ApiResult.Success -> _badgeCount.value = result.data
                else -> Unit
            }
        }
    }
}
