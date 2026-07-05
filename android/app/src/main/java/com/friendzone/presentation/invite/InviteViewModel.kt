package com.example.friendzone.presentation.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.BuildConfig
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.repository.UserRepository
import com.example.friendzone.domain.result.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InviteUiState(
    val username: String? = null,
    val inviteLink: String? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class InviteViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InviteUiState())
    val uiState: StateFlow<InviteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                user?.username?.takeIf { it.isNotBlank() }?.let { applyUsername(it) }
            }
        }
        loadMe()
    }

    private fun loadMe() {
        viewModelScope.launch {
            when (val result = userRepository.getMe()) {
                is ApiResult.Success -> applyUsername(result.data.username)
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false) }
                ApiResult.Loading -> Unit
            }
        }
    }

    private fun applyUsername(username: String) {
        if (username.isBlank()) return
        _uiState.update {
            it.copy(
                username = username,
                inviteLink = buildInviteLink(username),
                isLoading = false,
            )
        }
    }

    /** Message shared through the native share sheet (WhatsApp, Instagram, etc.). */
    fun shareMessage(link: String): String =
        "Join me on FriendZone! Add me as a friend with this link: $link"

    private fun buildInviteLink(username: String): String =
        // API_BASE_URL always ends with "/" (normalized in build.gradle.kts).
        "${BuildConfig.API_BASE_URL}invite/$username"
}
