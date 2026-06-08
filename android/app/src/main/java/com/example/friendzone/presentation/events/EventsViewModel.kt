package com.example.friendzone.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.data.remote.websocket.EventSocketManager
import com.example.friendzone.data.remote.websocket.SocketEventType
import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val eventSocketManager: EventSocketManager,
) : ViewModel() {
    private val _eventsState = MutableStateFlow<ApiResult<List<Event>>>(ApiResult.Loading)
    val eventsState: StateFlow<ApiResult<List<Event>>> = _eventsState.asStateFlow()

    init {
        viewModelScope.launch {
            eventSocketManager.connect()
            eventSocketManager.events.collect { event ->
                if (
                    event.type == SocketEventType.PARTICIPANT_JOINED ||
                    event.type == SocketEventType.EVENT_COMPLETED
                ) {
                    loadEvents()
                }
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            _eventsState.value = ApiResult.Loading
            _eventsState.value = when (val result = eventRepository.getMine()) {
                is ApiResult.Error -> ApiResult.Error(result.error)
                is ApiResult.Success -> result
                ApiResult.Loading -> ApiResult.Loading
            }
        }
    }

    fun errorMessage(): String? = (_eventsState.value as? ApiResult.Error)?.error?.displayMessage()
}
