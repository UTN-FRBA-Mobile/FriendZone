package com.example.friendzone.presentation.events

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.presentation.components.EventMapDialog
import com.example.friendzone.presentation.components.EventMapPerson
import com.example.friendzone.presentation.components.EventMapThumbnail
import com.example.friendzone.presentation.components.FriendRow
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.PillBadge
import com.example.friendzone.presentation.components.PillVariant
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzGreen
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface

@Composable
fun EventDetailScreen(
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inviteSheetOpen by viewModel.inviteSheetOpen.collectAsStateWithLifecycle()
    val isSharingLocation by viewModel.isSharingLocation.collectAsStateWithLifecycle()
    val sharingMessage by viewModel.sharingMessage.collectAsStateWithLifecycle()
    val deleteEventState by viewModel.deleteEventState.collectAsStateWithLifecycle()
    val leaveEventState by viewModel.leaveEventState.collectAsStateWithLifecycle()
    var mapOpen by remember { mutableStateOf(viewModel.shouldOpenMapOnLoad()) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showLeaveConfirmation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(sharingMessage) {
        sharingMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.consumeSharingMessage()
        }
    }

    LaunchedEffect(deleteEventState) {
        if (deleteEventState is EventActionState.Success) {
            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
            onBack()
        } else if (deleteEventState is EventActionState.Error) {
            Toast.makeText(
                context,
                (deleteEventState as EventActionState.Error).message,
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetDeleteEventState()
        }
    }

    LaunchedEffect(leaveEventState) {
        if (leaveEventState is EventActionState.Success) {
            Toast.makeText(context, "Left event", Toast.LENGTH_SHORT).show()
            onBack()
        } else if (leaveEventState is EventActionState.Error) {
            Toast.makeText(
                context,
                (leaveEventState as EventActionState.Error).message,
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetLeaveEventState()
        }
    }

    if (inviteSheetOpen) {
        InviteGuestsBottomSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.closeInviteSheet() },
        )
    }

    if (showDeleteConfirmation) {
        ConfirmDeleteEventDialog(
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.deleteEvent()
            },
            onDismiss = { showDeleteConfirmation = false },
            isLoading = deleteEventState is EventActionState.Loading,
        )
    }

    if (showLeaveConfirmation) {
        ConfirmLeaveEventDialog(
            onConfirm = {
                showLeaveConfirmation = false
                viewModel.leaveEvent()
            },
            onDismiss = { showLeaveConfirmation = false },
            isLoading = leaveEventState is EventActionState.Loading,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FzBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        CreateEventHeader(title = "Event", onBackClick = onBack)

        when (val state = uiState) {
            is EventDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = FzInk)
                }
            }
            is EventDetailUiState.Error -> {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.message, color = FzInk3)
                    TextButton(onClick = { viewModel.loadDetail() }) {
                        Text("Retry", color = FzInk)
                    }
                }
            }
            is EventDetailUiState.Data -> {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        state.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = FzInk,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        state.dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = FzInk3,
                    )
                    if (state.isLive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(FzGreen),
                            )
                            Text(
                                "Live",
                                style = MaterialTheme.typography.labelMedium,
                                color = FzInk,
                            )
                        }
                    }
                     if (state.canInviteGuests) {
                         Spacer(modifier = Modifier.height(16.dp))
                         FriendZoneOutlineButton(
                             text = "Add guests",
                             onClick = { viewModel.openInviteSheet() },
                         )
                         if (state.pendingInviteCount > 0) {
                             Spacer(modifier = Modifier.height(6.dp))
                             Text(
                                 "${state.pendingInviteCount} pending",
                                 style = MaterialTheme.typography.bodySmall,
                                 color = FzInk3,
                             )
                         }
                     }
                     if (state.isOrganizer) {
                         Spacer(modifier = Modifier.height(16.dp))
                         FriendZoneOutlineButton(
                             text = "Delete Event",
                             onClick = { showDeleteConfirmation = true },
                         )
                     } else {
                         Spacer(modifier = Modifier.height(16.dp))
                         FriendZoneOutlineButton(
                             text = "Leave Event",
                             onClick = { showLeaveConfirmation = true },
                         )
                     }
                    Spacer(modifier = Modifier.height(16.dp))
                    EventMapThumbnail(
                        eventLatitude = state.eventLatitude,
                        eventLongitude = state.eventLongitude,
                        onClick = { mapOpen = true },
                    )
                    if (mapOpen) {
                        EventMapDialog(
                            eventLatitude = state.eventLatitude,
                            eventLongitude = state.eventLongitude,
                            eventLabel = state.eventLocationLabel ?: state.title,
                            people = state.participantLocations.map { person ->
                                EventMapPerson(
                                    label = person.displayName,
                                    latitude = person.latitude,
                                    longitude = person.longitude,
                                    arrived = person.arrived,
                                )
                            },
                            isSharingLocation = isSharingLocation,
                            onSharingChange = viewModel::setLocationSharing,
                            onDismiss = { mapOpen = false },
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    ParticipantSection(state.arrived)
                    Spacer(modifier = Modifier.height(12.dp))
                    ParticipantSection(state.inTransit)
                    Spacer(modifier = Modifier.height(12.dp))
                    ParticipantSection(state.delayed)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ParticipantSection(section: ParticipantSectionUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                section.title,
                style = MaterialTheme.typography.labelLarge,
                color = FzInk,
                modifier = Modifier.weight(1f),
            )
            PillBadge("${section.count}", PillVariant.Light)
        }
        if (section.rows.isEmpty()) {
            Text(
                "None",
                style = MaterialTheme.typography.bodySmall,
                color = FzInk3,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else {
            section.rows.forEach { row ->
                HorizontalDivider(color = FzBorder)
                FriendRow(row)
            }
        }
    }
}
