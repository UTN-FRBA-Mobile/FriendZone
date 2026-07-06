package com.example.friendzone.presentation.friends

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.R
import com.example.friendzone.domain.model.FriendRequestStatus
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.repository.UserRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FriendsTab {
    Friends,
    Requests,
}

sealed class LookupResult {
    data class Found(val user: User) : LookupResult()
    data object NotFound : LookupResult()
    data class Error(val message: String) : LookupResult()
}

data class FriendsUiState(
    val selectedTab: FriendsTab = FriendsTab.Friends,
    val isLoading: Boolean = true,
    val friends: List<User> = emptyList(),
    val requests: List<com.example.friendzone.domain.model.FriendRequest> = emptyList(),
    val searchQuery: String = "",
    val lookupResult: LookupResult? = null,
    val isSendingRequest: Boolean = false,
    val snackbarMessage: String? = null,
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            val hasContent = _uiState.value.friends.isNotEmpty() || _uiState.value.requests.isNotEmpty()
            loadAllInternal(showFullLoading = !hasContent)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            try {
                val hasContent = !_uiState.value.isLoading &&
                    (_uiState.value.friends.isNotEmpty() || _uiState.value.requests.isNotEmpty())
                loadAllInternal(showFullLoading = !hasContent)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun loadAllInternal(showFullLoading: Boolean) {
        val cachedFriends = friendRepository.getCachedFriends()
        val cachedRequests = friendRepository.getCachedIncomingRequests()
        if (cachedFriends != null || cachedRequests != null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                friends = cachedFriends.orEmpty(),
                requests = cachedRequests.orEmpty(),
            )
        } else if (showFullLoading) {
            _uiState.value = _uiState.value.copy(isLoading = true)
        }

        val friends = when (val result = friendRepository.getFriends()) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> {
                if (cachedFriends == null) {
                    showMessage(result.error.displayMessage(context))
                }
                cachedFriends.orEmpty()
            }
            ApiResult.Loading -> cachedFriends.orEmpty()
        }
        val requests = when (val result = friendRepository.getIncomingRequests()) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> {
                if (cachedRequests == null && cachedFriends == null) {
                    showMessage(result.error.displayMessage(context))
                }
                cachedRequests.orEmpty()
            }
            ApiResult.Loading -> cachedRequests.orEmpty()
        }
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            friends = friends,
            requests = requests,
        )
    }

    fun selectTab(tab: FriendsTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            lookupResult = null,
        )
    }

    fun lookupUser() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            when (val result = userRepository.lookup(query)) {
                is ApiResult.Success -> {
                    val user = result.data
                    val alreadyFriend = _uiState.value.friends.any { it.id == user.id }
                    val pendingFromUser = _uiState.value.requests.any { it.requester.id == user.id }
                    when {
                        alreadyFriend -> {
                            _uiState.value = _uiState.value.copy(
                                lookupResult = LookupResult.Error(context.getString(R.string.msg_already_friends_with_name, user.displayName)),
                            )
                        }
                        pendingFromUser -> {
                            _uiState.value = _uiState.value.copy(
                                lookupResult = LookupResult.Error(context.getString(R.string.msg_invited_to_be_friend, user.displayName)),
                            )
                        }
                        else -> {
                            _uiState.value = _uiState.value.copy(
                                lookupResult = LookupResult.Found(user),
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        lookupResult = LookupResult.NotFound,
                    )
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun sendFriendRequest(emailOrUsername: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingRequest = true)
            when (val result = friendRepository.sendRequest(emailOrUsername)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        lookupResult = null,
                        searchQuery = "",
                        snackbarMessage = context.getString(R.string.msg_friend_request_sent),
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        snackbarMessage = result.error.displayMessage(context),
                    )
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun respondToRequest(requestId: String, accept: Boolean) {
        viewModelScope.launch {
            val status = if (accept) FriendRequestStatus.ACCEPTED else FriendRequestStatus.REJECTED
            when (val result = friendRepository.respondToRequest(requestId, status)) {
                is ApiResult.Success -> {
                    showMessage(if (accept) context.getString(R.string.msg_friend_added) else context.getString(R.string.msg_request_rejected))
                    loadAllInternal(showFullLoading = false)
                }
                is ApiResult.Error -> showMessage(result.error.displayMessage(context))
                ApiResult.Loading -> Unit
            }
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(snackbarMessage = message)
    }
}
