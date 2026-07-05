package com.example.friendzone.presentation.events

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.presentation.components.EventMapDialog
import com.example.friendzone.presentation.components.EventMapPerson
import com.example.friendzone.presentation.components.EventMapThumbnail
import com.example.friendzone.presentation.components.FriendRow
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePullToRefreshBox
import com.example.friendzone.presentation.components.PillBadge
import com.example.friendzone.presentation.components.PillVariant
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzSuccess
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import com.example.friendzone.ui.theme.FzError
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
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val showCompletePrompt by viewModel.showCompletePrompt.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var mapOpen by remember { mutableStateOf(viewModel.shouldOpenMapOnLoad()) }
    var menuOpen by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showLeaveConfirmation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(sharingMessage) {
        sharingMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.consumeSharingMessage()
        }
    }
    LaunchedEffect(actionMessage) {
        actionMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeActionMessage()
        }
    }

    LaunchedEffect(deleteEventState) {
        val state = deleteEventState
        if (state is EventActionState.Success) {
            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
            onBack()
        } else if (state is EventActionState.Error) {
            Toast.makeText(
                context,
                state.message,
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetDeleteEventState()
        }
    }

    LaunchedEffect(leaveEventState) {
        val state = leaveEventState
        if (state is EventActionState.Success) {
            Toast.makeText(context, "Left event", Toast.LENGTH_SHORT).show()
            onBack()
        } else if (state is EventActionState.Error) {
            Toast.makeText(
                context,
                state.message,
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

    if (showCompletePrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCompletePrompt() },
            title = { Text("Mark event as completed?") },
            text = {
                Text(
                    "This event has started and you have accepted guests. " +
                        "Do you want to mark it as completed?",
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmCompleteFromPrompt() }) {
                    Text("Mark completed", color = FzTextMain)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCompletePrompt() }) {
                    Text("Not now", color = FzTextSecondary)
                }
            },
        )
    }

    val organizerState = uiState as? EventDetailUiState.Data

    FriendZonePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FzBackground)
                .verticalScroll(rememberScrollState()),
        ) {
            CreateEventHeader(
                title = "Event",
                onBackClick = onBack,
                showMenu = organizerState?.showOrganizerMenu == true,
                onMenuClick = { menuOpen = true },
                menuExpanded = menuOpen,
                onMenuDismiss = { menuOpen = false },
                menuContent = if (organizerState?.showOrganizerMenu == true) {
                    {
                        if (organizerState.canInviteGuests) {
                            DropdownMenuItem(
                                text = { Text("Add guests") },
                                onClick = {
                                    menuOpen = false
                                    viewModel.openInviteSheet()
                                },
                            )
                        }
                        if (organizerState.canMarkComplete) {
                            DropdownMenuItem(
                                text = { Text("Mark completed") },
                                onClick = {
                                    menuOpen = false
                                    viewModel.markEventCompleted()
                                },
                            )
                        }
                        if (organizerState.canCancelEvent) {
                            DropdownMenuItem(
                                text = { Text("Cancel event", color = FzError) },
                                onClick = {
                                    menuOpen = false
                                    viewModel.cancelEvent()
                                },
                            )
                        }
                    }
                } else {
                    null
                },
            )

            when (val state = uiState) {
                is EventDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = FzTextMain)
                    }
                }
                is EventDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(state.message, color = FzTextSecondary)
                        TextButton(onClick = { viewModel.loadDetail() }) {
                            Text("Retry", color = FzTextMain)
                        }
                    }
                }
                is EventDetailUiState.Data -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        state.coverImageUrl?.let { coverUrl ->
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = "Event cover",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Text(
                            state.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = FzTextMain,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            state.dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FzTextSecondary,
                        )
                        when {
                            state.isLive -> EventStatusIndicatorRow(
                                dotColor = FzSuccess,
                                label = "Live now",
                            )
                            state.statusBadge == EventDetailStatusBadge.Completed -> EventStatusIndicatorRow(
                                dotColor = FzTextSecondary,
                                label = "Completed",
                            )
                            state.statusBadge == EventDetailStatusBadge.Cancelled -> EventStatusIndicatorRow(
                                dotColor = FzError,
                                label = "Cancelled",
                            )
                        }
                        if (state.organizerSelfArrived) {
                            Spacer(modifier = Modifier.height(12.dp))
                            PillBadge("You are already there", PillVariant.Green)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
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

                        // Action Buttons
                        Spacer(modifier = Modifier.height(24.dp))
                        if (state.isOrganizer) {
                            FriendZoneOutlineButton(
                                text = "Delete Event",
                                onClick = { showDeleteConfirmation = true },
                                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = FzError) }
                            )
                        } else {
                            FriendZoneOutlineButton(
                                text = "Leave Event",
                                onClick = { showLeaveConfirmation = true },
                                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = FzError) }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        if (state.invitedPending.rows.isNotEmpty()) {
                            ParticipantSection(state.invitedPending)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        ParticipantSection(state.arrived)
                        Spacer(modifier = Modifier.height(12.dp))
                        ParticipantSection(state.inTransit)
                        Spacer(modifier = Modifier.height(12.dp))
                        ParticipantSection(state.delayed)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EventStatusIndicatorRow(
    dotColor: Color,
    label: String,
) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(50))
                .background(dotColor),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = dotColor,
        )
    }
}

@Composable
private fun ParticipantSection(section: ParticipantSectionUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface)
            .border(1.dp, FzBorderGray, RoundedCornerShape(16.dp)),
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
                color = FzTextMain,
                modifier = Modifier.weight(1f),
            )
            PillBadge("${section.count}", PillVariant.Light)
        }
        if (section.rows.isEmpty()) {
            Text(
                "No participants yet",
                style = MaterialTheme.typography.bodySmall,
                color = FzTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else {
            section.rows.forEach { row ->
                HorizontalDivider(color = FzBorderGray)
                FriendRow(row)
            }
        }
    }
}
