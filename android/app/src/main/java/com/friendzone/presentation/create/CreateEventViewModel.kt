package com.example.friendzone.presentation.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendzone.data.location.GeocoderHelper
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
    val latitude: Double? = null,
    val longitude: Double? = null,
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val description: String = "",
    val guestLimit: String = "",
    val trackingLeadMinutes: Int = 30,
    val isCustomTracking: Boolean = false,
    val coverImageBytes: ByteArray? = null,
    val coverMimeType: String? = null,
    val coverPreviewUri: String? = null,
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
    private val geocoder: GeocoderHelper,
) : ViewModel() {
    private val _draft = MutableStateFlow(CreateEventDraft())
    val draft: StateFlow<CreateEventDraft> = _draft.asStateFlow()

    private val _locationMessage = MutableStateFlow<String?>(null)
    val locationMessage: StateFlow<String?> = _locationMessage.asStateFlow()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends.asStateFlow()

    private val _selectedFriendIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedFriendIds: StateFlow<Set<String>> = _selectedFriendIds.asStateFlow()

    private val _submitState = MutableStateFlow<CreateEventSubmitState>(CreateEventSubmitState.Idle)
    val submitState: StateFlow<CreateEventSubmitState> = _submitState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadFriends() {
        viewModelScope.launch {
            loadFriendsInternal()
        }
    }

    fun refreshFriends() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            try {
                loadFriendsInternal()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun loadFriendsInternal() {
        when (val result = friendRepository.getFriends()) {
            is ApiResult.Success -> _friends.value = result.data
            else -> Unit
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

    /**
     * Guarda la ubicacion elegida marcandola en el mapa. Setea las coordenadas
     * al instante (mostrando los numeros) y luego intenta resolver una
     * direccion legible para reemplazar el texto.
     */
    fun updatePickedLocation(latitude: Double, longitude: Double) {
        _draft.value = _draft.value.copy(
            latitude = latitude,
            longitude = longitude,
            location = "%.5f, %.5f".format(latitude, longitude),
        )
        viewModelScope.launch {
            val address = geocoder.reverseGeocode(latitude, longitude) ?: return@launch
            val current = _draft.value
            // Solo si el punto sigue siendo el mismo (no lo volvio a mover).
            if (current.latitude == latitude && current.longitude == longitude) {
                _draft.value = current.copy(location = address)
            }
        }
    }

    /**
     * Toma la direccion escrita en el campo y obtiene sus coordenadas. Si la
     * encuentra, las guarda y normaliza el texto; si no, avisa.
     */
    fun geocodeTypedLocation() {
        val query = _draft.value.location.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            when (val place = geocoder.forwardGeocode(query)) {
                null -> _locationMessage.value = "No se encontro la direccion"
                else -> {
                    _draft.value = _draft.value.copy(
                        latitude = place.latitude,
                        longitude = place.longitude,
                        location = place.address,
                    )
                    _locationMessage.value = "Ubicacion encontrada"
                }
            }
        }
    }

    fun consumeLocationMessage() {
        _locationMessage.value = null
    }

    fun updateGuestLimit(value: String) {
        _draft.value = _draft.value.copy(guestLimit = value)
    }

    fun setCoverImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri)
            if (mimeType != "image/jpeg" && mimeType != "image/png") {
                _locationMessage.value = "Only JPEG and PNG images are allowed"
                return@launch
            }
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
            if (bytes.size > 20 * 1024 * 1024) {
                _locationMessage.value = "Cover image must be 20 MB or smaller"
                return@launch
            }
            _draft.value = _draft.value.copy(
                coverImageBytes = bytes,
                coverMimeType = mimeType,
                coverPreviewUri = uri.toString(),
            )
        }
    }

    fun updateDescription(value: String) {
        _draft.value = _draft.value.copy(description = value)
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
                    latitude = d.latitude ?: PLACEHOLDER_LATITUDE,
                    longitude = d.longitude ?: PLACEHOLDER_LONGITUDE,
                    address = d.location.trim(),
                    startsAt = startsAt.toString(),
                    arrivalThresholdM = null,
                    trackingLeadMinutes = d.trackingLeadMinutes,
                )
            ) {
                is ApiResult.Success -> {
                    val eventId = result.data.id
                    if (d.coverImageBytes != null && d.coverMimeType != null) {
                        eventRepository.uploadCover(eventId, d.coverImageBytes, d.coverMimeType)
                    }
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
