package com.example.friendzone.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.data.notifications.FcmTokenRegistrar
import com.example.friendzone.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val fcmTokenRegistrar: FcmTokenRegistrar,
) : ViewModel() {
    private val loggedInFlow = authRepository.isLoggedIn

    val authSession: StateFlow<AuthSessionState> = loggedInFlow
        .map { loggedIn ->
            if (loggedIn) AuthSessionState.LoggedIn else AuthSessionState.LoggedOut
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthSessionState.Loading)

    val isLoggedIn: StateFlow<Boolean> = authSession
        .map { it == AuthSessionState.LoggedIn }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            loggedInFlow.distinctUntilChanged().collect { loggedIn ->
                if (loggedIn) {
                    fcmTokenRegistrar.syncCurrentToken()
                }
            }
        }
    }
}
