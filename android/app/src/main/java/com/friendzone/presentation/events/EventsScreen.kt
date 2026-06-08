package com.example.friendzone.presentation.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onEventDetailClick: (String) -> Unit,
    onNotificationsClick: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

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
                            onViewMapClick = { onEventDetailClick(item.eventId) },
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
                            onArrowClick = { onEventDetailClick(item.eventId) },
                        )
                    }
                }
            }
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
