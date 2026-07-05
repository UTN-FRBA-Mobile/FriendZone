package com.example.friendzone.presentation.navigation

sealed class AuthSessionState {
    data object Loading : AuthSessionState()
    data object LoggedOut : AuthSessionState()
    data object LoggedIn : AuthSessionState()
}
