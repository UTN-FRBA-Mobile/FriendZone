package com.example.friendzone.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun onDisplayNameChange(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun register() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.value = state.copy(errorMessage = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            when (
                val result = authRepository.register(
                    email = state.email.trim(),
                    username = state.username.trim(),
                    password = state.password,
                    displayName = state.displayName.trim(),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error.displayMessage(),
                    )
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    private fun validate(state: RegisterUiState): String? {
        if (state.email.isBlank() || state.username.isBlank() ||
            state.displayName.isBlank() || state.password.isBlank()
        ) {
            return "All fields are required."
        }
        if (!state.email.contains("@")) {
            return "Enter a valid email address."
        }
        if (!USERNAME_REGEX.matches(state.username)) {
            return "Username may only contain letters, numbers, and underscores."
        }
        if (state.username.length < 3) {
            return "Username must be at least 3 characters."
        }
        if (state.password.length < 8) {
            return "Password must be at least 8 characters."
        }
        if (state.password != state.confirmPassword) {
            return "Passwords do not match."
        }
        return null
    }

    companion object {
        private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]+$")
    }
}
