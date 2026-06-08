package com.example.friendzone.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.result.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsBadgeViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
) : ViewModel() {
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            when (val result = friendRepository.getPendingIncomingCount()) {
                is ApiResult.Success -> _pendingCount.value = result.data
                else -> Unit
            }
        }
    }
}
