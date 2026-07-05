package com.example.friendzone.presentation.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.repository.UserRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IncomingInviteUiState(
    val isLoading: Boolean = true,
    val inviter: User? = null,
    val errorMessage: String? = null,
    val isSelf: Boolean = false,
    val alreadyFriend: Boolean = false,
    val isAdding: Boolean = false,
    val added: Boolean = false,
)

@HiltViewModel
class IncomingInviteViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomingInviteUiState())
    val uiState: StateFlow<IncomingInviteUiState> = _uiState.asStateFlow()

    private var loadedUsername: String? = null

    fun load(username: String) {
        if (loadedUsername == username) return
        loadedUsername = username
        _uiState.value = IncomingInviteUiState(isLoading = true)
        viewModelScope.launch {
            val me = authRepository.currentUser.first()

            // You can't add yourself through your own link.
            if (me != null && me.username.equals(username, ignoreCase = true)) {
                _uiState.update { it.copy(isLoading = false, isSelf = true) }
                return@launch
            }

            when (val result = userRepository.lookup(username)) {
                is ApiResult.Success -> {
                    val inviter = result.data
                    val isSelf = me != null && me.id == inviter.id
                    val alreadyFriend = !isSelf && isAlreadyFriend(inviter.id)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            inviter = inviter,
                            isSelf = isSelf,
                            alreadyFriend = alreadyFriend,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.displayMessage())
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    private suspend fun isAlreadyFriend(userId: String): Boolean =
        when (val result = friendRepository.getFriends()) {
            is ApiResult.Success -> result.data.any { it.id == userId }
            else -> false
        }

    fun add() {
        val inviter = _uiState.value.inviter ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAdding = true, errorMessage = null) }
            when (val result = friendRepository.addByInvite(inviter.username)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isAdding = false, added = true)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isAdding = false, errorMessage = result.error.displayMessage())
                }
                ApiResult.Loading -> Unit
            }
        }
    }
}
