package com.example.friendzone.presentation.events

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.R
import com.example.friendzone.domain.model.PendingInvitation
import com.example.friendzone.domain.util.formatEventDate
import com.example.friendzone.presentation.components.EventLiveCard
import com.example.friendzone.presentation.components.EventUpcomingCard
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZonePullToRefreshBox
import com.example.friendzone.presentation.components.FriendZoneTopBar
import com.example.friendzone.presentation.components.PillBadge
import com.example.friendzone.presentation.components.PillVariant
import com.example.friendzone.ui.theme.Background
import com.example.friendzone.ui.theme.BorderGray
import com.example.friendzone.ui.theme.Primary
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary
import com.example.friendzone.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventsScreen(
    onCreateClick: () -> Unit,
    onEventDetailClick: (String, Boolean) -> Unit = { _, _ -> },
    onNotificationsClick: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    initialTab: EventsTab? = null,
    openInvitationId: String? = null,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedInvitation by viewModel.selectedInvitation.collectAsStateWithLifecycle()
    val isInvitationActionLoading by viewModel.isInvitationActionLoading.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pagerState = rememberPagerState(
        initialPage = selectedTab.ordinal,
        pageCount = { EventsTab.entries.size },
    )

    var eventIdToConfirmAction by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    val context = LocalContext.current

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val tab = EventsTab.entries[page]
            if (tab != selectedTab) {
                viewModel.selectTab(tab)
            }
        }
    }

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab.ordinal) {
            pagerState.animateScrollToPage(selectedTab.ordinal)
        }
    }

    LaunchedEffect(actionState) {
        val state = actionState
        when (state) {
            is EventActionState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetActionState()
            }
            is EventActionState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetActionState()
            }
            else -> Unit
        }
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

    if (eventIdToConfirmAction != null) {
        val (eventId, isDelete) = eventIdToConfirmAction!!
        if (isDelete) {
            ConfirmDeleteEventDialog(
                onConfirm = {
                    viewModel.deleteEvent(eventId)
                    eventIdToConfirmAction = null
                },
                onDismiss = { eventIdToConfirmAction = null },
                isLoading = actionState is EventActionState.Loading,
            )
        } else {
            ConfirmLeaveEventDialog(
                onConfirm = {
                    viewModel.leaveEvent(eventId)
                    eventIdToConfirmAction = null
                },
                onDismiss = { eventIdToConfirmAction = null },
                isLoading = actionState is EventActionState.Loading,
            )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
        ) {
            FriendZoneTopBar(
                title = stringResource(R.string.header_events),
                showNotifications = true,
                notificationBadgeCount = notificationBadgeCount,
                onNotificationsClick = onNotificationsClick,
            )
            EventsSegmentedControl(
                selectedTab = selectedTab,
                invitationCount = (uiState as? EventsUiState.Data)?.pendingInvitations?.size ?: 0,
                onTabSelected = viewModel::selectTab,
            )

            FriendZonePullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (val state = uiState) {
                    is EventsUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                    is EventsUiState.Error -> {
                        ColumnError(
                            message = state.message,
                            onRetry = { viewModel.loadEvents() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    is EventsUiState.Data -> {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            when (EventsTab.entries[page]) {
                                EventsTab.Upcoming -> UpcomingEventsPage(
                                    events = state.upcomingEvents,
                                    onEventDetailClick = onEventDetailClick,
                                    onDeleteClick = { eventIdToConfirmAction = it to true },
                                    onLeaveClick = { eventIdToConfirmAction = it to false },
                                )
                                EventsTab.Past -> PastEventsPage(
                                    events = state.pastEvents,
                                    onEventDetailClick = onEventDetailClick,
                                    onDeleteClick = { eventIdToConfirmAction = it to true },
                                    onLeaveClick = { eventIdToConfirmAction = it to false },
                                )
                                EventsTab.Invitations -> InvitationsPage(
                                    invitations = state.pendingInvitations,
                                    onInvitationClick = viewModel::openInvitation,
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Primary,
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.header_create_event), tint = Color.White)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun UpcomingEventsPage(
    events: List<EventListItemUi>,
    onEventDetailClick: (String, Boolean) -> Unit,
    onDeleteClick: (String) -> Unit,
    onLeaveClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        if (events.isEmpty()) {
            item { EmptyTabMessage(stringResource(R.string.msg_no_upcoming_events)) }
        } else {
            items(events, key = { it.eventId }) { item ->
                if (item.isLive) {
                    EventLiveCard(
                        item = item,
                        onClick = { onEventDetailClick(item.eventId, false) },
                        onViewMapClick = { onEventDetailClick(item.eventId, true) },
                        onDeleteClick = { onDeleteClick(item.eventId) },
                        onLeaveClick = { onLeaveClick(item.eventId) },
                    )
                } else {
                    EventUpcomingCard(
                        item = item,
                        onClick = { onEventDetailClick(item.eventId, false) },
                        onDeleteClick = { onDeleteClick(item.eventId) },
                        onLeaveClick = { onLeaveClick(item.eventId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PastEventsPage(
    events: List<EventListItemUi>,
    onEventDetailClick: (String, Boolean) -> Unit,
    onDeleteClick: (String) -> Unit,
    onLeaveClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        if (events.isEmpty()) {
            item { EmptyTabMessage(stringResource(R.string.msg_no_past_events)) }
        } else {
            items(events, key = { it.eventId }) { item ->
                EventUpcomingCard(
                    item = item,
                    onClick = { onEventDetailClick(item.eventId, false) },
                    onDeleteClick = { onDeleteClick(item.eventId) },
                    onLeaveClick = { onLeaveClick(item.eventId) },
                )
            }
        }
    }
}

@Composable
private fun InvitationsPage(
    invitations: List<PendingInvitation>,
    onInvitationClick: (PendingInvitation) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        if (invitations.isEmpty()) {
            item { EmptyTabMessage(stringResource(R.string.msg_no_invites)) }
        } else {
            items(invitations, key = { it.id }) { invitation ->
                InvitationRow(
                    invitation = invitation,
                    onClick = { onInvitationClick(invitation) },
                )
            }
        }
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
        EventsTabChip(stringResource(R.string.tab_upcoming), selectedTab == EventsTab.Upcoming, { onTabSelected(EventsTab.Upcoming) }, Modifier.weight(1f))
        EventsTabChip(stringResource(R.string.tab_past), selectedTab == EventsTab.Past, { onTabSelected(EventsTab.Past) }, Modifier.weight(1f))
        val inviteLabel = if (invitationCount > 0) "${stringResource(R.string.tab_invites)} ($invitationCount)" else stringResource(R.string.tab_invites)
        EventsTabChip(
            inviteLabel,
            selected = selectedTab == EventsTab.Invitations,
            onClick = { onTabSelected(EventsTab.Invitations) },
            modifier = Modifier.weight(1f),
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
            .background(if (selected) Primary else Surface)
            .border(1.dp, if (selected) Primary else BorderGray, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else TextSecondary,
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
            .background(Surface)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(invitation.eventTitle, style = MaterialTheme.typography.titleMedium, color = TextMain)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.msg_invitation_from, invitation.organizerDisplayName, formatEventDate(invitation.eventStartsAt)),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PillBadge(stringResource(R.string.tab_invites), PillVariant.Light)
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
        Text(stringResource(R.string.tab_invites), style = MaterialTheme.typography.titleMedium, color = TextMain)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.msg_invitation_body, invitation.eventTitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.msg_invitation_from, invitation.organizerDisplayName, formatEventDate(invitation.eventStartsAt)),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FriendZoneOutlineButton(
                text = if (isLoading) "..." else stringResource(R.string.btn_reject),
                onClick = onReject,
                modifier = Modifier.weight(1f),
            )
            FriendZonePrimaryButton(
                text = if (isLoading) "..." else stringResource(R.string.btn_accept),
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
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

@Composable
private fun ColumnError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.btn_retry), color = Primary)
            }
        }
    }
}
