package com.example.friendzone.presentation.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.data.remote.websocket.EventSocketManager
import com.example.friendzone.data.remote.websocket.SocketEventType
import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.model.Invitation
import com.example.friendzone.domain.model.InvitationStatus
import com.example.friendzone.domain.model.ParticipantWithUser
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.AuthRepository
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.repository.InvitationRepository
import com.example.friendzone.domain.repository.LocationRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import com.example.friendzone.domain.util.ParticipantStatus
import com.example.friendzone.domain.util.classifyParticipantWithUser
import com.example.friendzone.domain.util.formatEventDate
import com.example.friendzone.domain.util.isLive
import com.example.friendzone.presentation.components.FriendRowUi
import com.example.friendzone.presentation.components.PillVariant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParticipantSectionUi(
    val title: String,
    val count: Int,
    val rows: List<FriendRowUi>,
)

data class InvitedGuestUi(
    val displayName: String,
    val statusLabel: String,
    val pillVariant: PillVariant,
)

sealed class EventDetailUiState {
    data object Loading : EventDetailUiState()
    data class Error(val message: String) : EventDetailUiState()
    data class Data(
        val title: String,
        val dateText: String,
        val isLive: Boolean,
        val canInviteGuests: Boolean,
        val pendingInviteCount: Int,
        val arrived: ParticipantSectionUi,
        val inTransit: ParticipantSectionUi,
        val delayed: ParticipantSectionUi,
    ) : EventDetailUiState()
}

sealed class InviteSubmitState {
    data object Idle : InviteSubmitState()
    data object Loading : InviteSubmitState()
    data class Success(val message: String) : InviteSubmitState()
    data class Error(val message: String) : InviteSubmitState()
}

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val locationRepository: LocationRepository,
    private val eventSocketManager: EventSocketManager,
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository,
    private val invitationRepository: InvitationRepository,
) : ViewModel() {
    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private val _inviteSheetOpen = MutableStateFlow(false)
    val inviteSheetOpen: StateFlow<Boolean> = _inviteSheetOpen.asStateFlow()

    private val _inviteFriends = MutableStateFlow<List<User>>(emptyList())
    val inviteFriends: StateFlow<List<User>> = _inviteFriends.asStateFlow()

    private val _selectedInviteFriendIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedInviteFriendIds: StateFlow<Set<String>> = _selectedInviteFriendIds.asStateFlow()

    private val _pendingInvites = MutableStateFlow<List<InvitedGuestUi>>(emptyList())
    val pendingInvites: StateFlow<List<InvitedGuestUi>> = _pendingInvites.asStateFlow()

    private val _inviteSubmitState = MutableStateFlow<InviteSubmitState>(InviteSubmitState.Idle)
    val inviteSubmitState: StateFlow<InviteSubmitState> = _inviteSubmitState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                currentUserId = user?.id
            }
        }
        viewModelScope.launch {
            eventSocketManager.connect()
            eventSocketManager.joinEvent(eventId)
            eventSocketManager.events.collect { event ->
                if (
                    event.type == SocketEventType.LOCATION_UPDATED ||
                    event.type == SocketEventType.PARTICIPANT_ARRIVED ||
                    event.type == SocketEventType.PARTICIPANT_JOINED ||
                    event.type == SocketEventType.EVENT_COMPLETED
                ) {
                    loadDetail()
                }
            }
        }
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            when (val eventResult = eventRepository.getById(eventId)) {
                is ApiResult.Error -> {
                    _uiState.value = EventDetailUiState.Error(eventResult.error.displayMessage())
                }
                is ApiResult.Success -> {
                    val event = eventResult.data
                    val invitations = loadInvitationsIfOrganizer(event)
                    when (val participantsResult = locationRepository.getParticipants(eventId)) {
                        is ApiResult.Error -> {
                            _uiState.value = EventDetailUiState.Error(
                                participantsResult.error.displayMessage(),
                            )
                        }
                        is ApiResult.Success -> {
                            _uiState.value = buildUiState(
                                event = event,
                                participants = participantsResult.data,
                                invitations = invitations,
                            )
                        }
                        ApiResult.Loading -> Unit
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun openInviteSheet() {
        _inviteSheetOpen.value = true
        _selectedInviteFriendIds.value = emptySet()
        _inviteSubmitState.value = InviteSubmitState.Idle
        loadInviteData()
    }

    fun closeInviteSheet() {
        _inviteSheetOpen.value = false
        _selectedInviteFriendIds.value = emptySet()
        _inviteSubmitState.value = InviteSubmitState.Idle
    }

    fun toggleInviteFriendSelection(friendId: String) {
        _selectedInviteFriendIds.value = _selectedInviteFriendIds.value.toMutableSet().apply {
            if (contains(friendId)) remove(friendId) else add(friendId)
        }
    }

    fun resetInviteSubmitState() {
        _inviteSubmitState.value = InviteSubmitState.Idle
    }

    fun loadInviteData() {
        viewModelScope.launch {
            val friends = when (val result = friendRepository.getFriends()) {
                is ApiResult.Success -> result.data
                else -> emptyList()
            }
            val invitations = when (val result = invitationRepository.getByEvent(eventId)) {
                is ApiResult.Success -> result.data
                else -> emptyList()
            }
            applyInviteLists(friends, invitations)
        }
    }

    fun sendInvites() {
        val selectedIds = _selectedInviteFriendIds.value
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _inviteSubmitState.value = InviteSubmitState.Loading
            val selected = _inviteFriends.value.filter { it.id in selectedIds }
            val failedNames = mutableListOf<String>()
            var successCount = 0

            selected.forEach { friend ->
                when (val result = invitationRepository.create(eventId, friend.username)) {
                    is ApiResult.Success -> successCount++
                    is ApiResult.Error -> failedNames.add(friend.displayName)
                    ApiResult.Loading -> Unit
                }
            }

            loadInviteData()
            loadDetail()

            _inviteSubmitState.value = when {
                failedNames.isEmpty() -> {
                    _selectedInviteFriendIds.value = emptySet()
                    InviteSubmitState.Success(
                        if (successCount == 1) "Invite sent" else "$successCount invites sent",
                    )
                }
                successCount == 0 -> InviteSubmitState.Error(
                    "Could not send invites: ${failedNames.joinToString()}",
                )
                else -> InviteSubmitState.Success(
                    "Sent $successCount invite(s). Failed: ${failedNames.joinToString()}",
                )
            }
        }
    }

    private suspend fun loadInvitationsIfOrganizer(event: Event): List<Invitation> {
        if (!isOrganizer(event)) return emptyList()
        return when (val result = invitationRepository.getByEvent(eventId)) {
            is ApiResult.Success -> result.data
            else -> emptyList()
        }
    }

    private fun isOrganizer(event: Event): Boolean =
        currentUserId != null && event.organizerId == currentUserId

    private fun canInviteGuests(event: Event): Boolean =
        isOrganizer(event) &&
            (event.status == EventStatus.SCHEDULED || event.status == EventStatus.ACTIVE)

    private fun applyInviteLists(friends: List<User>, invitations: List<Invitation>) {
        val invitedIds = invitations.map { it.inviteeId }.toSet()
        _inviteFriends.value = friends.filter { it.id !in invitedIds }
        _pendingInvites.value = invitations.map { invitation ->
            val friend = friends.find { it.id == invitation.inviteeId }
            invitationToGuestUi(
                displayName = friend?.displayName ?: "Guest",
                status = invitation.status,
            )
        }
    }

    private fun buildUiState(
        event: Event,
        participants: List<ParticipantWithUser>,
        invitations: List<Invitation>,
    ): EventDetailUiState.Data {
        val arrived = mutableListOf<FriendRowUi>()
        val inTransit = mutableListOf<FriendRowUi>()
        val delayed = mutableListOf<FriendRowUi>()

        participants.forEach { item ->
            val row = when (val status = classifyParticipantWithUser(item, event)) {
                is ParticipantStatus.Arrived -> participantToFriendRow(
                    displayName = item.user.displayName,
                    subtitle = "Arrived",
                    pillText = "✓ Arrived",
                    pillVariant = PillVariant.Dark,
                )
                is ParticipantStatus.InTransit -> participantToFriendRow(
                    displayName = item.user.displayName,
                    subtitle = status.etaMinutes?.let { "$it min away" } ?: "On the way",
                    pillText = status.etaMinutes?.let { "✈ $it min" } ?: "In route",
                    pillVariant = PillVariant.Light,
                )
                is ParticipantStatus.Delayed -> participantToFriendRow(
                    displayName = item.user.displayName,
                    subtitle = status.etaMinutes?.let { "$it min away" } ?: "Running late",
                    pillText = status.etaMinutes?.let { "🕐 $it min" } ?: "Delayed",
                    pillVariant = PillVariant.Amber,
                )
            }
            when (classifyParticipantWithUser(item, event)) {
                is ParticipantStatus.Arrived -> arrived.add(row)
                is ParticipantStatus.InTransit -> inTransit.add(row)
                is ParticipantStatus.Delayed -> delayed.add(row)
            }
        }

        val pendingCount = invitations.count { it.status == InvitationStatus.PENDING }

        return EventDetailUiState.Data(
            title = event.title,
            dateText = formatEventDate(event.startsAt),
            isLive = event.isLive(),
            canInviteGuests = canInviteGuests(event),
            pendingInviteCount = pendingCount,
            arrived = ParticipantSectionUi("Arrived", arrived.size, arrived),
            inTransit = ParticipantSectionUi("In transit", inTransit.size, inTransit),
            delayed = ParticipantSectionUi("Delayed", delayed.size, delayed),
        )
    }

    override fun onCleared() {
        eventSocketManager.leaveEvent(eventId)
        super.onCleared()
    }
}

private fun invitationToGuestUi(
    displayName: String,
    status: InvitationStatus,
): InvitedGuestUi = when (status) {
    InvitationStatus.PENDING -> InvitedGuestUi(displayName, "Pending", PillVariant.Light)
    InvitationStatus.ACCEPTED -> InvitedGuestUi(displayName, "Accepted", PillVariant.Green)
    InvitationStatus.REJECTED -> InvitedGuestUi(displayName, "Rejected", PillVariant.Amber)
}
