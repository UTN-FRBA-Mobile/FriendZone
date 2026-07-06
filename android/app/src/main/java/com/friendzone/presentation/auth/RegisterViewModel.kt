package com.example.friendzone.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.R
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
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
                        errorMessage = result.error.displayMessage(context),
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
            return context.getString(R.string.error_all_fields_required)
        }
        if (!state.email.contains("@")) {
            return context.getString(R.string.error_invalid_email)
        }
        if (!USERNAME_REGEX.matches(state.username)) {
            return context.getString(R.string.error_username_format)
        }
        if (state.username.length < 3) {
            return context.getString(R.string.error_username_length)
        }
        if (state.password.length < 8) {
            return context.getString(R.string.error_password_length)
        }
        if (state.password != state.confirmPassword) {
            return context.getString(R.string.error_passwords_dont_match)
        }
        return null
    }

    companion object {
        private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]+$")
    }
}
