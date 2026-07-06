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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.friendzone.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZonePullToRefreshBox
import com.example.friendzone.presentation.components.InviteFriendChip
import com.example.friendzone.presentation.components.StepProgressBar
import com.example.friendzone.ui.theme.Background
import com.example.friendzone.ui.theme.Primary
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary

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
                    .background(Background)
                    .verticalScroll(rememberScrollState()),
            ) {
                    CreateEventHeader(title = stringResource(R.string.header_create_event), onBackClick = onBack)
                StepProgressBar(
                    stepLabel = stringResource(R.string.create_step_label, 2, 2),
                    stepDescription = stringResource(R.string.create_step2_desc),
                    progress = 1f,
                )

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        stringResource(R.string.header_invite_friends),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMain,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.create_invite_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (friends.isEmpty()) {
                        Text(
                            stringResource(R.string.create_no_friends_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
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
                    Spacer(modifier = Modifier.height(32.dp))
                    FriendZonePrimaryButton(
                        text = if (isLoading) stringResource(R.string.btn_creating) else stringResource(R.string.btn_create_event),
                        onClick = { viewModel.createEvent() },
                        enabled = !isLoading,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary)
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
