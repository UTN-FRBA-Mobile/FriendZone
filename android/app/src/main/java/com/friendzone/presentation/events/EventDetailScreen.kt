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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.friendzone.R
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.presentation.components.SkeletonBox
import com.example.friendzone.presentation.components.EventMapDialog
import com.example.friendzone.presentation.components.EventMapPerson
import com.example.friendzone.presentation.components.EventMapThumbnail
import com.example.friendzone.presentation.components.FriendRow
import com.example.friendzone.presentation.components.FriendRowUi
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePullToRefreshBox
import com.example.friendzone.presentation.components.PillBadge
import com.example.friendzone.presentation.components.PillVariant
import com.example.friendzone.ui.theme.Background
import com.example.friendzone.ui.theme.BorderGray
import com.example.friendzone.ui.theme.Success
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary
import com.example.friendzone.ui.theme.ErrorColor
import com.example.friendzone.ui.theme.Surface

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
            Toast.makeText(context, context.getString(R.string.msg_event_deleted), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, context.getString(R.string.msg_left_event), Toast.LENGTH_SHORT).show()
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
            title = { Text(stringResource(R.string.msg_mark_completed_title)) },
            text = {
                Text(stringResource(R.string.msg_mark_completed_desc))
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmCompleteFromPrompt() }) {
                    Text(stringResource(R.string.msg_mark_completed), color = TextMain)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCompletePrompt() }) {
                    Text(stringResource(R.string.msg_not_now), color = TextSecondary)
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
                .background(Background)
                .verticalScroll(rememberScrollState()),
        ) {
            CreateEventHeader(
                title = stringResource(R.string.header_event),
                onBackClick = onBack,
                showMenu = organizerState?.showOrganizerMenu == true,
                onMenuClick = { menuOpen = true },
                menuExpanded = menuOpen,
                onMenuDismiss = { menuOpen = false },
                menuContent = if (organizerState?.showOrganizerMenu == true) {
                    {
                        if (organizerState.canInviteGuests) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.msg_add_guests)) },
                                onClick = {
                                    menuOpen = false
                                    viewModel.openInviteSheet()
                                },
                            )
                        }
                        if (organizerState.canMarkComplete) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.msg_mark_completed)) },
                                onClick = {
                                    menuOpen = false
                                    viewModel.markEventCompleted()
                                },
                            )
                        }
                        if (organizerState.canCancelEvent) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.msg_cancel_event), color = ErrorColor) },
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
                        CircularProgressIndicator(color = TextMain)
                    }
                }
                is EventDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(state.message, color = TextSecondary)
                        TextButton(onClick = { viewModel.loadDetail() }) {
                            Text(stringResource(R.string.btn_retry), color = TextMain)
                        }
                    }
                }
                is EventDetailUiState.Data -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        state.coverImageUrl?.let { coverUrl ->
                            SubcomposeAsyncImage(
                                model = coverUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    SkeletonBox(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                },
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Text(
                            state.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextMain,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            state.dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                        when {
                            state.isLive -> EventStatusIndicatorRow(
                                dotColor = Success,
                                label = stringResource(R.string.msg_live_now),
                            )
                            state.statusBadge == EventDetailStatusBadge.Completed -> EventStatusIndicatorRow(
                                dotColor = TextSecondary,
                                label = stringResource(R.string.msg_completed),
                            )
                            state.statusBadge == EventDetailStatusBadge.Cancelled -> EventStatusIndicatorRow(
                                dotColor = ErrorColor,
                                label = stringResource(R.string.msg_cancelled),
                            )
                        }
                        if (state.organizerSelfArrived) {
                            Spacer(modifier = Modifier.height(12.dp))
                            PillBadge(stringResource(R.string.msg_already_there), PillVariant.Green)
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
                                text = stringResource(R.string.btn_delete),
                                onClick = { showDeleteConfirmation = true },
                                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorColor) }
                            )
                        } else {
                            FriendZoneOutlineButton(
                                text = stringResource(R.string.btn_leave),
                                onClick = { showLeaveConfirmation = true },
                                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = ErrorColor) }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        if (state.invitedPending.rows.isNotEmpty()) {
                            ParticipantSection(
                                title = stringResource(R.string.tab_invited_pending),
                                count = state.invitedPending.count,
                                rows = state.invitedPending.rows
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        ParticipantSection(
                            title = stringResource(R.string.tab_arrived),
                            count = state.arrived.count,
                            rows = state.arrived.rows
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ParticipantSection(
                            title = stringResource(R.string.tab_in_transit),
                            count = state.inTransit.count,
                            rows = state.inTransit.rows
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ParticipantSection(
                            title = stringResource(R.string.tab_delayed),
                            count = state.delayed.count,
                            rows = state.delayed.rows
                        )
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
private fun ParticipantSection(
    title: String,
    count: Int,
    rows: List<FriendRowUi>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = TextMain,
                modifier = Modifier.weight(1f),
            )
            PillBadge("$count", PillVariant.Light)
        }
        if (rows.isEmpty()) {
            Text(
                stringResource(R.string.msg_no_participants),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else {
            rows.forEach { row ->
                HorizontalDivider(color = BorderGray)
                FriendRow(row)
            }
        }
    }
}
