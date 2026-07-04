package com.example.friendzone.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.data.local.TokenManager
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.repository.UserRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val friendsCount: Int? = null,
    val eventsCount: Int? = null,
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isUpdatingLocationSharing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
    private val eventRepository: EventRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { cachedUser ->
                _uiState.update { state ->
                    state.copy(user = cachedUser ?: state.user)
                }
            }
        }
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val meResult = userRepository.getMe()) {
                is ApiResult.Success -> {
                    tokenManager.saveUserProfile(meResult.data)
                    _uiState.update { it.copy(user = meResult.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = meResult.error.displayMessage())
                    }
                }
                ApiResult.Loading -> Unit
            }

            val friendsCount = when (val friendsResult = friendRepository.getFriends()) {
                is ApiResult.Success -> friendsResult.data.size
                is ApiResult.Error -> null
                ApiResult.Loading -> null
            }

            val eventsCount = when (val eventsResult = eventRepository.getMine()) {
                is ApiResult.Success -> eventsResult.data.size
                is ApiResult.Error -> null
                ApiResult.Loading -> null
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    friendsCount = friendsCount,
                    eventsCount = eventsCount,
                )
            }
        }
    }

    fun setLocationSharing(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingLocationSharing = true, errorMessage = null) }
            when (val result = userRepository.updateLocationSharing(enabled)) {
                is ApiResult.Success -> {
                    tokenManager.saveUserProfile(result.data)
                    _uiState.update {
                        it.copy(
                            user = result.data,
                            isUpdatingLocationSharing = false,
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingLocationSharing = false,
                            errorMessage = result.error.displayMessage(),
                        )
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun showLocationPermissionRequired() {
        _uiState.update {
            it.copy(errorMessage = "Location permission is required to share your location")
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, errorMessage = null) }
            when (val result = authRepository.logout()) {
                is ApiResult.Success -> {
                    _uiState.value = ProfileUiState()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            errorMessage = result.error.displayMessage(),
                        )
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }
}
