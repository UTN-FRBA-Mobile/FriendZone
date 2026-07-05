package com.example.friendzone.presentation.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.friendzone.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.domain.model.AppNotificationType
import com.example.friendzone.domain.model.InboxNotification
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZonePullToRefreshBox
import com.example.friendzone.presentation.components.PillBadge
import com.example.friendzone.presentation.components.PillVariant
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import com.example.friendzone.ui.theme.FzSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onActionFinished: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionFinished by viewModel.actionFinished.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadInbox()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    LaunchedEffect(actionFinished) {
        if (actionFinished) {
            viewModel.resetActionFinished()
            onActionFinished()
        }
    }

    uiState.selectedNotification?.let { notification ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissSheet() },
            sheetState = sheetState,
        ) {
            NotificationActionSheet(
                notification = notification,
                subtitle = viewModel.detailSubtitle(notification),
                isLoading = uiState.isActionLoading,
                onAccept = { viewModel.acceptSelected() },
                onReject = { viewModel.rejectSelected() },
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FriendZonePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FzBackground),
            ) {
                item {
                    CreateEventHeader(title = stringResource(R.string.header_notifications), onBackClick = onBack)
                }

                when {
                    uiState.isLoading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = FzPrimary)
                            }
                        }
                    }
                    uiState.errorMessage != null -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(uiState.errorMessage!!, color = FzTextSecondary)
                                TextButton(onClick = { viewModel.loadInbox() }) {
                                    Text("Retry", color = FzPrimary)
                                }
                            }
                        }
                    }
                    uiState.items.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(stringResource(R.string.msg_no_notifications), color = FzTextSecondary)
                            }
                        }
                    }
                    else -> {
                        items(uiState.items, key = { it.id }) { item ->
                            NotificationRow(
                                item = item,
                                onClick = { viewModel.selectNotification(item) },
                            )
                            HorizontalDivider(
                                color = FzBorderGray,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
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

@Composable
private fun NotificationRow(
    item: InboxNotification,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.labelLarge, color = FzTextMain)
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.body, style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
        }
        if (item.actionable) {
            Spacer(modifier = Modifier.padding(start = 8.dp))
            PillBadge("Action", PillVariant.Light)
        }
    }
}

@Composable
private fun NotificationActionSheet(
    notification: InboxNotification,
    subtitle: String?,
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
        Text(
            stringResource(R.string.title_invitation),
            style = MaterialTheme.typography.titleMedium,
            color = FzTextMain,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.msg_invitation_body, notification.data["eventTitle"] ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            color = FzTextSecondary,
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
        }
        if (notification.type == AppNotificationType.INVITATION_CREATED) {
            notification.data["eventTitle"]?.let { eventTitle ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    eventTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = FzTextMain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FzSurface)
                        .border(1.dp, FzBorderGray, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FriendZoneOutlineButton(
                text = if (isLoading) "..." else stringResource(R.string.btn_decline),
                onClick = onReject,
                modifier = Modifier.weight(1f),
            )
            FriendZonePrimaryButton(
                text = when {
                    isLoading -> "..."
                    notification.type == AppNotificationType.INVITATION_CREATED -> stringResource(R.string.btn_accept_invite)
                    else -> stringResource(R.string.btn_accept)
                },
                onClick = onAccept,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
