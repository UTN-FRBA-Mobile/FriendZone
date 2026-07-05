package com.example.friendzone.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.domain.model.FriendRequestStatus
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.repository.UserRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val friends = when (val result = friendRepository.getFriends()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> {
                    showMessage(result.error.displayMessage())
                    emptyList()
                }
                ApiResult.Loading -> emptyList()
            }
            val requests = when (val result = friendRepository.getIncomingRequests()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> {
                    showMessage(result.error.displayMessage())
                    emptyList()
                }
                ApiResult.Loading -> emptyList()
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                friends = friends,
                requests = requests,
            )
        }
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
                                lookupResult = LookupResult.Error("Already friends"),
                            )
                        }
                        pendingFromUser -> {
                            _uiState.value = _uiState.value.copy(
                                lookupResult = LookupResult.Error("This user already sent you a request"),
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
                        snackbarMessage = "Friend request sent",
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        snackbarMessage = result.error.displayMessage(),
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
                    showMessage(if (accept) "Friend added" else "Request rejected")
                    loadAll()
                }
                is ApiResult.Error -> showMessage(result.error.displayMessage())
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
