package com.example.friendzone.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.data.notifications.FcmTokenRegistrar
import com.example.friendzone.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val fcmTokenRegistrar: FcmTokenRegistrar,
) : ViewModel() {
    private val loggedInFlow = authRepository.isLoggedIn

    val isLoggedIn: StateFlow<Boolean> = loggedInFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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
