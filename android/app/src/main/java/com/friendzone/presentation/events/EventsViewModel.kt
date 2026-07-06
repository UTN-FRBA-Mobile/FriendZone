package com.example.friendzone.presentation.events

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.BuildConfig
import com.example.friendzone.data.remote.websocket.EventSocketManager
import com.example.friendzone.data.remote.websocket.SocketEventType
import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.domain.model.PendingInvitation
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.repository.InvitationRepository
import com.example.friendzone.domain.repository.LocationRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import com.example.friendzone.domain.util.ParticipantStatus
import com.example.friendzone.domain.util.classifyParticipantWithUser
import com.example.friendzone.domain.util.isLive
import com.example.friendzone.domain.util.isPastEvent
import com.example.friendzone.domain.util.parseStartsAt
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

enum class EventsTab {
    Upcoming,
    Past,
    Invitations,
}

sealed class EventsUiState {
    data object Loading : EventsUiState()
    data class Error(val message: String) : EventsUiState()
    data class Data(
        val upcomingEvents: List<EventListItemUi>,
        val pastEvents: List<EventListItemUi>,
        val pendingInvitations: List<PendingInvitation>,
    ) : EventsUiState()
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val invitationRepository: InvitationRepository,
    private val locationRepository: LocationRepository,
    private val eventSocketManager: EventSocketManager,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<EventActionState>(EventActionState.Idle)
    val actionState: StateFlow<EventActionState> = _actionState.asStateFlow()

    private val _selectedTab = MutableStateFlow(EventsTab.Upcoming)
    val selectedTab: StateFlow<EventsTab> = _selectedTab.asStateFlow()

    private val _selectedInvitation = MutableStateFlow<PendingInvitation?>(null)
    val selectedInvitation: StateFlow<PendingInvitation?> = _selectedInvitation.asStateFlow()

    private val _isInvitationActionLoading = MutableStateFlow(false)
    val isInvitationActionLoading: StateFlow<Boolean> = _isInvitationActionLoading.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                currentUserId = user?.id
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
                    loadEventsInternal(showFullLoading = false)
                }
            }
        }
    }

    fun selectTab(tab: EventsTab) {
        _selectedTab.value = tab
    }

    fun openInvitation(invitation: PendingInvitation) {
        _selectedInvitation.value = invitation
    }

    fun dismissInvitationSheet() {
        _selectedInvitation.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun openInvitationById(invitationId: String) {
        val state = _uiState.value as? EventsUiState.Data ?: return
        state.pendingInvitations.find { it.id == invitationId }?.let {
            _selectedTab.value = EventsTab.Invitations
            _selectedInvitation.value = it
        }
    }

    fun respondToSelectedInvitation(accept: Boolean) {
        val invitation = _selectedInvitation.value ?: return
        viewModelScope.launch {
            _isInvitationActionLoading.value = true
            when (
                val result = invitationRepository.respond(
                    invitation.id,
                    if (accept) InvitationStatus.ACCEPTED else InvitationStatus.REJECTED,
                )
            ) {
                is ApiResult.Success -> {
                    _isInvitationActionLoading.value = false
                    _selectedInvitation.value = null
                    _snackbarMessage.value = if (accept) "Joined event" else "Declined"
                    loadEvents()
                }
                is ApiResult.Error -> {
                    _isInvitationActionLoading.value = false
                    _snackbarMessage.value = result.error.displayMessage()
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            loadEventsInternal(showFullLoading = _uiState.value !is EventsUiState.Data)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            try {
                loadEventsInternal(
                    showFullLoading = _uiState.value !is EventsUiState.Data,
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun loadEventsInternal(showFullLoading: Boolean) {
        val cachedEvents = eventRepository.getCachedMine()
        val cachedInvitations = invitationRepository.getCachedMinePending()
        if (cachedEvents != null) {
            val enriched = enrichEvents(cachedEvents, useCache = true)
            val upcoming = enriched
                .filter { !it.isPastItem }
                .sortedWith(
                    compareByDescending<EventListItemUi> { it.isLive }
                        .thenBy { it.startsAtEpoch },
                )
            val past = enriched
                .filter { it.isPastItem }
                .sortedByDescending { it.startsAtEpoch }
            _uiState.value = EventsUiState.Data(
                upcomingEvents = upcoming,
                pastEvents = past,
                pendingInvitations = cachedInvitations.orEmpty(),
            )
        } else if (showFullLoading) {
            _uiState.value = EventsUiState.Loading
        }

        val eventsResult = eventRepository.getMine()
        val invitationsResult = invitationRepository.getMinePending()

        if (eventsResult is ApiResult.Error) {
            if (_uiState.value !is EventsUiState.Data) {
                _uiState.value = EventsUiState.Error(eventsResult.error.displayMessage())
            }
            return
        }

        val events = (eventsResult as ApiResult.Success).data
        val pendingInvitations = when (invitationsResult) {
            is ApiResult.Success -> invitationsResult.data
            else -> cachedInvitations.orEmpty()
        }

        val enriched = enrichEvents(events, useCache = false)
        val upcoming = enriched
            .filter { !it.isPastItem }
            .sortedWith(
                compareByDescending<EventListItemUi> { it.isLive }
                    .thenBy { it.startsAtEpoch },
            )
        val past = enriched
            .filter { it.isPastItem }
            .sortedByDescending { it.startsAtEpoch }

        _uiState.value = EventsUiState.Data(
            upcomingEvents = upcoming,
            pastEvents = past,
            pendingInvitations = pendingInvitations,
        )

        _selectedInvitation.update { selected ->
            selected?.let { current ->
                pendingInvitations.find { it.id == current.id }
            }
        }
    }

    private suspend fun enrichEvents(events: List<Event>, useCache: Boolean): List<EventListItemUi> = coroutineScope {
        events.map { event ->
            async {
                val invitations = if (useCache) {
                    invitationRepository.getCachedByEvent(event.id)
                        ?: when (val result = invitationRepository.getByEvent(event.id)) {
                            is ApiResult.Success -> result.data
                            else -> emptyList()
                        }
                } else {
                    when (val result = invitationRepository.getByEvent(event.id)) {
                        is ApiResult.Success -> result.data
                        else -> emptyList()
                    }
                }
                val (confirmed, pending) = countInvitations(invitations)

                val participants = if (useCache) {
                    locationRepository.getCachedParticipants(event.id)
                        ?: when (val p = locationRepository.getParticipants(event.id)) {
                            is ApiResult.Success -> p.data
                            else -> emptyList()
                        }
                } else {
                    when (val p = locationRepository.getParticipants(event.id)) {
                        is ApiResult.Success -> p.data
                        else -> emptyList()
                    }
                }

                val baseItem = if (event.isLive()) {
                    val onTheWay = participants.count { item ->
                        classifyParticipantWithUser(item, event) is ParticipantStatus.InTransit ||
                            classifyParticipantWithUser(item, event) is ParticipantStatus.Delayed
                    }
                    event.toListItemUi(
                        context = context,
                        confirmedCount = confirmed,
                        pendingCount = pending,
                        onTheWayCount = onTheWay,
                        friendPreviews = buildFriendPreviews(context, event, participants),
                        isPastItem = event.isPastEvent(BuildConfig.EVENT_PAST_THRESHOLD_HOURS),
                        startsAtEpoch = event.parseStartsAt().epochSecond,
                        organizerId = event.organizerId,
                        currentUserId = currentUserId,
                    )
                } else {
                    val (avatars, extra) = buildAvatarPreview(participants)
                    event.toListItemUi(
                        context = context,
                        confirmedCount = confirmed,
                        pendingCount = pending,
                        participantAvatars = avatars,
                        extraAvatarCount = extra,
                        isPastItem = event.isPastEvent(BuildConfig.EVENT_PAST_THRESHOLD_HOURS),
                        startsAtEpoch = event.parseStartsAt().epochSecond,
                        organizerId = event.organizerId,
                        currentUserId = currentUserId,
                    )
                }
                baseItem
            }
        }.awaitAll()
    }

    private suspend fun loadInvitationsIfOrganizer(event: Event): List<com.example.friendzone.domain.model.Invitation> {
        if (!isOrganizer(event)) return emptyList()
        return when (val result = invitationRepository.getByEvent(event.id)) {
            is ApiResult.Success -> result.data
            else -> emptyList()
        }
    }

    private fun isOrganizer(event: Event): Boolean =
        currentUserId != null && event.organizerId == currentUserId

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
