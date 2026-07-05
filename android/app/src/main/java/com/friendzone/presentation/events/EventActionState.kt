package com.example.friendzone.presentation.events

sealed class EventActionState {
    data object Idle : EventActionState()
    data object Loading : EventActionState()
    data class Success(val message: String) : EventActionState()
    data class Error(val message: String) : EventActionState()
}
