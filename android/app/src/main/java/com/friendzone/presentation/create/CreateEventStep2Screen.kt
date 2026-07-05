package com.example.friendzone.presentation.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZonePullToRefreshBox
import com.example.friendzone.presentation.components.InviteFriendChip
import com.example.friendzone.presentation.components.StepProgressBar
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3

@Composable
fun CreateEventStep2Screen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateEventViewModel,
) {
    BackHandler(onBack = onBack)
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    val selectedFriendIds by viewModel.selectedFriendIds.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadFriends()
    }

    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is CreateEventSubmitState.Success -> {
                state.warning?.let { snackbarHostState.showSnackbar(it) }
                viewModel.resetSubmitState()
                onCreated()
            }
            is CreateEventSubmitState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetSubmitState()
            }
            else -> Unit
        }
    }

    val isLoading = submitState is CreateEventSubmitState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
        FriendZonePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refreshFriends,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FzBackground)
                    .verticalScroll(rememberScrollState()),
            ) {
            CreateEventHeader(title = "Create Event", onBackClick = onBack)
            StepProgressBar(
                stepLabel = "Step 2 of 2",
                stepDescription = "Invite Friends",
                progress = 1f,
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Invite friends",
                    style = MaterialTheme.typography.labelLarge,
                    color = FzInk,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tap friends to invite them to this event",
                    style = MaterialTheme.typography.bodySmall,
                    color = FzInk3,
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (friends.isEmpty()) {
                    Text(
                        "No friends yet. Add friends from the Friends tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FzInk3,
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(friends, key = { it.id }) { friend ->
                            InviteFriendChip(
                                friend = friend,
                                selected = friend.id in selectedFriendIds,
                                onClick = { viewModel.toggleFriendSelection(friend.id) },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                FriendZonePrimaryButton(
                    text = if (isLoading) "Creating..." else "Create Event",
                    onClick = { viewModel.createEvent() },
                    enabled = !isLoading,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        }
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = FzInk)
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}
