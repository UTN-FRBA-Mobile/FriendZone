package com.example.friendzone.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.domain.model.User
import com.example.friendzone.domain.repository.EventRepository
import com.example.friendzone.domain.repository.FriendRepository
import com.example.friendzone.domain.repository.InvitationRepository
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.displayMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class CreateEventDraft(
    val eventName: String = "",
    val location: String = "",
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val description: String = "",
    val guestLimit: String = "",
    val trackingLeadMinutes: Int = 30,
    val isCustomTracking: Boolean = false,
)

sealed class CreateEventSubmitState {
    data object Idle : CreateEventSubmitState()
    data object Loading : CreateEventSubmitState()
    data class Success(val warning: String? = null) : CreateEventSubmitState()
    data class Error(val message: String) : CreateEventSubmitState()
}

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val friendRepository: FriendRepository,
    private val invitationRepository: InvitationRepository,
) : ViewModel() {
    private val _draft = MutableStateFlow(CreateEventDraft())
    val draft: StateFlow<CreateEventDraft> = _draft.asStateFlow()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends.asStateFlow()

    private val _selectedFriendIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedFriendIds: StateFlow<Set<String>> = _selectedFriendIds.asStateFlow()

    private val _submitState = MutableStateFlow<CreateEventSubmitState>(CreateEventSubmitState.Idle)
    val submitState: StateFlow<CreateEventSubmitState> = _submitState.asStateFlow()

    fun loadFriends() {
        viewModelScope.launch {
            when (val result = friendRepository.getFriends()) {
                is ApiResult.Success -> _friends.value = result.data
                else -> Unit
            }
        }
    }

    fun toggleFriendSelection(friendId: String) {
        _selectedFriendIds.value = _selectedFriendIds.value.toMutableSet().apply {
            if (contains(friendId)) remove(friendId) else add(friendId)
        }
    }

    fun updateEventName(value: String) {
        _draft.value = _draft.value.copy(eventName = value)
    }

    fun updateLocation(value: String) {
        _draft.value = _draft.value.copy(location = value)
    }

    fun updateDescription(value: String) {
        _draft.value = _draft.value.copy(description = value)
    }

    fun updateGuestLimit(value: String) {
        _draft.value = _draft.value.copy(guestLimit = value)
    }

    fun updateDate(date: LocalDate) {
        _draft.value = _draft.value.copy(selectedDate = date)
    }

    fun updateTime(time: LocalTime) {
        _draft.value = _draft.value.copy(selectedTime = time)
    }

    fun selectTrackingPreset(minutes: Int) {
        _draft.value = _draft.value.copy(
            trackingLeadMinutes = minutes,
            isCustomTracking = false,
        )
    }

    fun selectCustomTracking(minutes: Int) {
        _draft.value = _draft.value.copy(
            trackingLeadMinutes = minutes.coerceIn(1, 1440),
            isCustomTracking = true,
        )
    }

    fun step1Valid(): Boolean {
        val d = _draft.value
        return d.eventName.isNotBlank() &&
            d.location.isNotBlank() &&
            d.selectedDate != null &&
            d.selectedTime != null
    }

    fun createEvent() {
        val d = _draft.value
        val date = d.selectedDate ?: return
        val time = d.selectedTime ?: return
        val startsAt = date.atTime(time)
            .atZone(ZoneId.systemDefault())
            .toInstant()

        viewModelScope.launch {
            _submitState.value = CreateEventSubmitState.Loading
            when (
                val result = eventRepository.create(
                    title = d.eventName.trim(),
                    description = d.description.trim().ifBlank { null },
                    latitude = PLACEHOLDER_LATITUDE,
                    longitude = PLACEHOLDER_LONGITUDE,
                    address = d.location.trim(),
                    startsAt = startsAt.toString(),
                    arrivalThresholdM = null,
                    trackingLeadMinutes = d.trackingLeadMinutes,
                )
            ) {
                is ApiResult.Success -> {
                    val eventId = result.data.id
                    val selected = _friends.value.filter {
                        _selectedFriendIds.value.contains(it.id)
                    }
                    val failedNames = mutableListOf<String>()
                    selected.forEach { friend ->
                        when (
                            val inviteResult = invitationRepository.create(
                                eventId,
                                friend.username,
                            )
                        ) {
                            is ApiResult.Error -> failedNames.add(friend.displayName)
                            else -> Unit
                        }
                    }
                    _submitState.value = CreateEventSubmitState.Success(
                        warning = if (failedNames.isEmpty()) {
                            null
                        } else {
                            "Event created, but could not invite: ${failedNames.joinToString()}"
                        },
                    )
                }
                is ApiResult.Error -> {
                    _submitState.value = CreateEventSubmitState.Error(
                        result.error.displayMessage(),
                    )
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = CreateEventSubmitState.Idle
    }

    fun formattedDate(): String =
        _draft.value.selectedDate?.toString() ?: ""

    fun formattedTime(): String =
        _draft.value.selectedTime?.let { "%02d:%02d".format(it.hour, it.minute) } ?: ""

    companion object {
        private const val PLACEHOLDER_LATITUDE = 40.7128
        private const val PLACEHOLDER_LONGITUDE = -74.0060
    }
}
