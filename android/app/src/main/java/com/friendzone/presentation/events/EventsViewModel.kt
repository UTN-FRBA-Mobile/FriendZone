package com.example.friendzone.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.data.remote.websocket.EventSocketManager
import com.example.friendzone.data.remote.websocket.SocketEventType
import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.repository.InvitationRepository
import com.example.friendzone.domain.repository.LocationRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import com.example.friendzone.domain.util.ParticipantStatus
import com.example.friendzone.domain.util.classifyParticipantWithUser
import com.example.friendzone.domain.util.isLive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EventsUiState {
    data object Loading : EventsUiState()
    data class Error(val message: String) : EventsUiState()
    data class Data(
        val liveEvents: List<EventListItemUi>,
        val upcomingEvents: List<EventListItemUi>,
    ) : EventsUiState()
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val invitationRepository: InvitationRepository,
    private val locationRepository: LocationRepository,
    private val eventSocketManager: EventSocketManager,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<EventActionState>(EventActionState.Idle)
    val actionState: StateFlow<EventActionState> = _actionState.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                currentUserId = user?.id
                // Cargar eventos DESPUÉS de obtener el usuario
                if (currentUserId != null) {
                    loadEvents()
                }
            }
        }
        viewModelScope.launch {
            eventSocketManager.connect()
            eventSocketManager.events.collect { event ->
                if (
                    event.type == SocketEventType.PARTICIPANT_JOINED ||
                    event.type == SocketEventType.EVENT_COMPLETED ||
                    event.type == SocketEventType.LOCATION_UPDATED ||
                    event.type == SocketEventType.PARTICIPANT_ARRIVED ||
                    event.type == SocketEventType.RECONNECTED
                ) {
                    loadEvents()
                }
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            when (val result = eventRepository.getMine()) {
                is ApiResult.Error -> {
                    _uiState.value = EventsUiState.Error(result.error.displayMessage())
                }
                is ApiResult.Success -> {
                    val events = result.data.filter {
                        it.status != EventStatus.COMPLETED && it.status != EventStatus.CANCELLED
                    }
                    val items = enrichEvents(events)
                    _uiState.value = EventsUiState.Data(
                        liveEvents = items.filter { it.isLive },
                        upcomingEvents = items.filter { !it.isLive },
                    )
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    private suspend fun enrichEvents(events: List<Event>): List<EventListItemUi> = coroutineScope {
        events.map { event ->
            async {
                val invitationsResult = invitationRepository.getByEvent(event.id)
                val (confirmed, pending) = when (invitationsResult) {
                    is ApiResult.Success -> countInvitations(invitationsResult.data)
                    else -> 0 to 0
                }

                if (event.isLive()) {
                    val participants = when (val p = locationRepository.getParticipants(event.id)) {
                        is ApiResult.Success -> p.data
                        else -> emptyList()
                    }
                    val onTheWay = participants.count { item ->
                        classifyParticipantWithUser(item, event) is ParticipantStatus.InTransit ||
                            classifyParticipantWithUser(item, event) is ParticipantStatus.Delayed
                    }
                    event.toListItemUi(
                        confirmedCount = confirmed,
                        pendingCount = pending,
                        onTheWayCount = onTheWay,
                        friendPreviews = buildFriendPreviews(event, participants),
                        organizerId = event.organizerId,
                        currentUserId = currentUserId,
                    )
                } else {
                    val participants = when (val p = locationRepository.getParticipants(event.id)) {
                        is ApiResult.Success -> p.data
                        else -> emptyList()
                    }
                    val (avatars, extra) = buildAvatarPreview(participants)
                    event.toListItemUi(
                        confirmedCount = confirmed,
                        pendingCount = pending,
                        participantAvatars = avatars,
                        extraAvatarCount = extra,
                        organizerId = event.organizerId,
                        currentUserId = currentUserId,
                    )
                }
            }
        }.awaitAll()
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _actionState.value = EventActionState.Loading
            when (val result = eventRepository.delete(eventId)) {
                is ApiResult.Success -> {
                    _actionState.value = EventActionState.Success("Event deleted")
                    loadEvents()
                }
                is ApiResult.Error -> {
                    _actionState.value = EventActionState.Error(result.error.displayMessage())
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun leaveEvent(eventId: String) {
        viewModelScope.launch {
            _actionState.value = EventActionState.Loading
            when (val result = eventRepository.leave(eventId)) {
                is ApiResult.Success -> {
                    _actionState.value = EventActionState.Success("Left event")
                    loadEvents()
                }
                is ApiResult.Error -> {
                    _actionState.value = EventActionState.Error(result.error.displayMessage())
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun resetActionState() {
        _actionState.value = EventActionState.Idle
    }
}
