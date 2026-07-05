package com.example.friendzone.presentation.friends

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.domain.model.FriendRequest
import com.example.friendzone.domain.model.User
import com.example.friendzone.presentation.components.FriendZoneTextField
import com.example.friendzone.presentation.components.FriendZoneTopBar
import com.example.friendzone.presentation.components.UserInitialAvatar
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface
import com.example.friendzone.ui.theme.FzSurface2

@Composable
fun FriendsScreen(
    onFriendsChanged: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    initialTab: FriendsTab? = null,
    viewModel: FriendsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAll()
        onFriendsChanged()
    }

    LaunchedEffect(initialTab) {
        initialTab?.let(viewModel::selectTab)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
            onFriendsChanged()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FzBackground),
        ) {
            FriendZoneTopBar(
                title = "Friends",
                showNotifications = true,
                notificationBadgeCount = notificationBadgeCount,
                onNotificationsClick = onNotificationsClick,
            )
            FriendsSegmentedControl(
                selectedTab = uiState.selectedTab,
                requestCount = uiState.requests.size,
                onTabSelected = viewModel::selectTab,
            )

            when (uiState.selectedTab) {
                FriendsTab.Friends -> FriendsListContent(
                    isLoading = uiState.isLoading,
                    searchQuery = uiState.searchQuery,
                    lookupResult = uiState.lookupResult,
                    isSendingRequest = uiState.isSendingRequest,
                    friends = uiState.friends,
                    onSearchChange = viewModel::updateSearchQuery,
                    onSearchSubmit = viewModel::lookupUser,
                    onSendRequest = { user ->
                        viewModel.sendFriendRequest(user.username)
                        onFriendsChanged()
                    },
                )
                FriendsTab.Requests -> RequestsListContent(
                    isLoading = uiState.isLoading,
                    requests = uiState.requests,
                    onAccept = { id ->
                        viewModel.respondToRequest(id, accept = true)
                        onFriendsChanged()
                    },
                    onReject = { id ->
                        viewModel.respondToRequest(id, accept = false)
                        onFriendsChanged()
                    },
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun FriendsSegmentedControl(
    selectedTab: FriendsTab,
    requestCount: Int,
    onTabSelected: (FriendsTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SegmentChip(
            label = "Friends",
            selected = selectedTab == FriendsTab.Friends,
            onClick = { onTabSelected(FriendsTab.Friends) },
            modifier = Modifier.weight(1f),
        )
        SegmentChip(
            label = if (requestCount > 0) "Requests ($requestCount)" else "Requests",
            selected = selectedTab == FriendsTab.Requests,
            onClick = { onTabSelected(FriendsTab.Requests) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SegmentChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) FzInk else FzSurface2
    val fg = if (selected) FzSurface else FzInk
    Text(
        text = label,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        style = MaterialTheme.typography.labelLarge,
        color = fg,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@Composable
private fun FriendsListContent(
    isLoading: Boolean,
    searchQuery: String,
    lookupResult: LookupResult?,
    isSendingRequest: Boolean,
    friends: List<User>,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onSendRequest: (User) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                FriendZoneTextField(
                    label = "Add friend",
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = "Email or username",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                )
                when (lookupResult) {
                    is LookupResult.Found -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        LookupUserRow(
                            user = lookupResult.user,
                            isSending = isSendingRequest,
                            onAddClick = { onSendRequest(lookupResult.user) },
                        )
                    }
                    is LookupResult.NotFound -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No user found", color = FzInk3, style = MaterialTheme.typography.bodySmall)
                    }
                    is LookupResult.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(lookupResult.message, color = FzInk3, style = MaterialTheme.typography.bodySmall)
                    }
                    null -> Unit
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your friends", style = MaterialTheme.typography.labelLarge, color = FzInk)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = FzInk)
                }
            }
        } else if (friends.isEmpty()) {
            item {
                Text(
                    "No friends yet",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = FzInk3,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            items(friends, key = { it.id }) { friend ->
                FriendListRow(friend)
            }
        }
    }
}

@Composable
private fun LookupUserRow(
    user: User,
    isSending: Boolean,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FzSurface)
            .border(1.5.dp, FzBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserInitialAvatar(displayName = user.displayName)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.displayName, style = MaterialTheme.typography.bodyMedium, color = FzInk)
            Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = FzInk3)
        }
        if (isSending) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = FzInk, strokeWidth = 2.dp)
        } else {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FzInk),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Send request", tint = FzSurface)
            }
        }
    }
}

@Composable
private fun FriendListRow(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(FzSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserInitialAvatar(displayName = user.displayName)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(user.displayName, style = MaterialTheme.typography.bodyMedium, color = FzInk)
            Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = FzInk3)
        }
    }
}

@Composable
private fun RequestsListContent(
    isLoading: Boolean,
    requests: List<FriendRequest>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = FzInk)
        }
        return
    }
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("No pending requests", color = FzInk3)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(requests, key = { it.id }) { request ->
            RequestRow(
                request = request,
                onAccept = { onAccept(request.id) },
                onReject = { onReject(request.id) },
            )
            HorizontalDivider(color = FzBorder, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun RequestRow(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserInitialAvatar(displayName = request.requester.displayName)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(request.requester.displayName, style = MaterialTheme.typography.bodyMedium, color = FzInk)
            Text("@${request.requester.username}", style = MaterialTheme.typography.bodySmall, color = FzInk3)
        }
        Text(
            "Accept",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(FzInk)
                .clickable(onClick = onAccept)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = FzSurface,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Reject",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, FzBorder, RoundedCornerShape(8.dp))
                .clickable(onClick = onReject)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = FzInk,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
