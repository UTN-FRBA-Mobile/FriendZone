package com.example.friendzone.presentation.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.domain.model.PendingInvitation
import com.example.friendzone.domain.util.formatEventDate
import com.example.friendzone.presentation.components.EventLiveCard
import com.example.friendzone.presentation.components.EventUpcomingCard
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZoneTopBar
import com.example.friendzone.presentation.components.PillBadge
import com.example.friendzone.presentation.components.PillVariant
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onCreateClick: () -> Unit,
    onEventDetailClick: (String) -> Unit,
    onNotificationsClick: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    initialTab: EventsTab? = null,
    openInvitationId: String? = null,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedInvitation by viewModel.selectedInvitation.collectAsStateWithLifecycle()
    val isInvitationActionLoading by viewModel.isInvitationActionLoading.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    LaunchedEffect(initialTab) {
        initialTab?.let(viewModel::selectTab)
    }

    LaunchedEffect(openInvitationId, uiState) {
        openInvitationId?.let(viewModel::openInvitationById)
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    selectedInvitation?.let { invitation ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissInvitationSheet() },
            sheetState = sheetState,
        ) {
            InvitationActionSheet(
                invitation = invitation,
                isLoading = isInvitationActionLoading,
                onAccept = { viewModel.respondToSelectedInvitation(true) },
                onReject = { viewModel.respondToSelectedInvitation(false) },
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FzBackground),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                FriendZoneTopBar(
                    title = "My Events",
                    showNotifications = true,
                    notificationBadgeCount = notificationBadgeCount,
                    onNotificationsClick = onNotificationsClick,
                    showAdd = true,
                    onAddClick = onCreateClick,
                )
            }
            item {
                EventsSegmentedControl(
                    selectedTab = selectedTab,
                    invitationCount = (uiState as? EventsUiState.Data)?.pendingInvitations?.size ?: 0,
                    onTabSelected = viewModel::selectTab,
                )
            }

            when (val state = uiState) {
                is EventsUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = FzInk)
                        }
                    }
                }
                is EventsUiState.Error -> {
                    item {
                        ColumnError(
                            message = state.message,
                            onRetry = { viewModel.loadEvents() },
                        )
                    }
                }
                is EventsUiState.Data -> when (selectedTab) {
                    EventsTab.Upcoming -> {
                        if (state.upcomingEvents.isEmpty()) {
                            item { EmptyTabMessage("No upcoming events") }
                        } else {
                            items(state.upcomingEvents, key = { it.eventId }) { item ->
                                if (item.isLive) {
                                    EventLiveCard(
                                        item = item,
                                        onViewMapClick = { onEventDetailClick(item.eventId) },
                                    )
                                } else {
                                    EventUpcomingCard(
                                        item = item,
                                        onArrowClick = { onEventDetailClick(item.eventId) },
                                    )
                                }
                            }
                        }
                    }
                    EventsTab.Past -> {
                        if (state.pastEvents.isEmpty()) {
                            item { EmptyTabMessage("No past events") }
                        } else {
                            items(state.pastEvents, key = { it.eventId }) { item ->
                                EventUpcomingCard(
                                    item = item,
                                    onArrowClick = { onEventDetailClick(item.eventId) },
                                )
                            }
                        }
                    }
                    EventsTab.Invitations -> {
                        if (state.pendingInvitations.isEmpty()) {
                            item { EmptyTabMessage("No pending invitations") }
                        } else {
                            items(state.pendingInvitations, key = { it.id }) { invitation ->
                                InvitationRow(
                                    invitation = invitation,
                                    onClick = { viewModel.openInvitation(invitation) },
                                )
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun EventsSegmentedControl(
    selectedTab: EventsTab,
    invitationCount: Int,
    onTabSelected: (EventsTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EventsTabChip("Upcoming", selectedTab == EventsTab.Upcoming, { onTabSelected(EventsTab.Upcoming) }, Modifier.weight(1f))
        EventsTabChip("Past", selectedTab == EventsTab.Past, { onTabSelected(EventsTab.Past) }, Modifier.weight(1f))
        EventsTabChip(
            if (invitationCount > 0) "Invites ($invitationCount)" else "Invites",
            selectedTab == EventsTab.Invitations,
            { onTabSelected(EventsTab.Invitations) },
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun EventsTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) FzInk else FzSurface)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) FzBackground else FzInk,
        )
    }
}

@Composable
private fun InvitationRow(
    invitation: PendingInvitation,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(invitation.eventTitle, style = MaterialTheme.typography.titleMedium, color = FzInk)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "From ${invitation.organizerDisplayName} · ${formatEventDate(invitation.eventStartsAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = FzInk3,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PillBadge("Pending", PillVariant.Light)
    }
}

@Composable
fun InvitationActionSheet(
    invitation: PendingInvitation,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        Text("Event Invitation", style = MaterialTheme.typography.titleMedium, color = FzInk)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "You have been invited to \"${invitation.eventTitle}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = FzInk3,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "From ${invitation.organizerDisplayName} · ${formatEventDate(invitation.eventStartsAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = FzInk3,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FriendZoneOutlineButton(
                text = if (isLoading) "..." else "Decline",
                onClick = onReject,
                modifier = Modifier.weight(1f),
            )
            FriendZonePrimaryButton(
                text = if (isLoading) "..." else "Accept invite",
                onClick = onAccept,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun EmptyTabMessage(message: String) {
    Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        color = FzInk3,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

@Composable
private fun ColumnError(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = FzInk3)
            TextButton(onClick = onRetry) {
                Text("Retry", color = FzInk)
            }
        }
    }
}
