package com.example.friendzone.presentation.events

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.EventLiveCard
import com.example.friendzone.presentation.components.EventUpcomingCard
import com.example.friendzone.presentation.components.FriendZoneTopBar
import com.example.friendzone.presentation.components.SectionLabel
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3

@Composable
fun EventsScreen(
    onCreateClick: () -> Unit,
    onEventDetailClick: (String, Boolean) -> Unit = { eventId, _ -> },
    onNotificationsClick: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()

    var eventIdToConfirmAction by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // ID to (isDelete)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is EventActionState.Success -> {
                Toast.makeText(context, (actionState as EventActionState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetActionState()
            }
            is EventActionState.Error -> {
                Toast.makeText(context, (actionState as EventActionState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetActionState()
            }
            else -> Unit
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
                is EventsUiState.Data -> {
                    if (state.liveEvents.isNotEmpty()) {
                        item {
                            SectionLabel("Next Events")
                        }
                        items(state.liveEvents, key = { it.eventId }) { item ->
                            EventLiveCard(
                                item = item,
                                onCardClick = { onEventDetailClick(item.eventId, false) },
                                onViewMapClick = { onEventDetailClick(item.eventId, true) },
                                onDeleteClick = { eventIdToConfirmAction = item.eventId to true },
                                onLeaveClick = { eventIdToConfirmAction = item.eventId to false },
                            )
                        }
                    }
                    item {
                        SectionLabel(
                            "Upcoming",
                            modifier = Modifier.padding(top = if (state.liveEvents.isEmpty()) 0.dp else 6.dp),
                        )
                    }
                    if (state.upcomingEvents.isEmpty()) {
                        item {
                            Text(
                                "No upcoming events",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FzInk3,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                    } else {
                        items(state.upcomingEvents, key = { it.eventId }) { item ->
                            EventUpcomingCard(
                                item = item,
                                onCardClick = { onEventDetailClick(item.eventId, false) },
                                onArrowClick = { onEventDetailClick(item.eventId, false) },
                                onDeleteClick = { eventIdToConfirmAction = item.eventId to true },
                                onLeaveClick = { eventIdToConfirmAction = item.eventId to false },
                            )
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
            containerColor = FzInk,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create event", tint = androidx.compose.ui.graphics.Color.White)
        }
    }
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
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = FzInk3)
            TextButton(onClick = onRetry) {
                Text("Retry", color = FzInk)
            }
        }
    }
}
