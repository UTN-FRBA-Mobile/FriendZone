package com.example.friendzone.presentation.friends

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.R
import com.example.friendzone.domain.model.FriendRequest
import com.example.friendzone.domain.model.User
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePullToRefreshBox
import com.example.friendzone.presentation.components.FriendZoneTextField
import com.example.friendzone.presentation.components.FriendZoneTopBar
import com.example.friendzone.presentation.components.UserInitialAvatar
import com.example.friendzone.presentation.invite.InviteFriendsBottomSheet
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import com.example.friendzone.ui.theme.FzSurface

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendsScreen(
    onFriendsChanged: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    initialTab: FriendsTab? = null,
    viewModel: FriendsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showInviteSheet by rememberSaveable { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTab.ordinal,
        pageCount = { FriendsTab.entries.size },
    )

    LaunchedEffect(Unit) {
        onFriendsChanged()
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val tab = FriendsTab.entries[page]
            if (tab != uiState.selectedTab) {
                viewModel.selectTab(tab)
            }
        }
    }

    LaunchedEffect(uiState.selectedTab) {
        if (pagerState.currentPage != uiState.selectedTab.ordinal) {
            pagerState.animateScrollToPage(uiState.selectedTab.ordinal)
        }
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
                title = stringResource(R.string.header_friends),
                showNotifications = true,
                notificationBadgeCount = notificationBadgeCount,
                onNotificationsClick = onNotificationsClick,
            )
            FriendsSegmentedControl(
                selectedTab = uiState.selectedTab,
                requestCount = uiState.requests.size,
                onTabSelected = viewModel::selectTab,
            )

            FriendZonePullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    viewModel.refresh()
                    onFriendsChanged()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    when (FriendsTab.entries[page]) {
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
                            onInviteClick = { showInviteSheet = true },
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
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        FloatingActionButton(
            onClick = { viewModel.selectTab(FriendsTab.Friends) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = FzPrimary,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add friend", tint = Color.White)
        }
    }

    if (showInviteSheet) {
        InviteFriendsBottomSheet(onDismiss = { showInviteSheet = false })
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
            label = stringResource(R.string.tab_friends),
            selected = selectedTab == FriendsTab.Friends,
            onClick = { onTabSelected(FriendsTab.Friends) },
            modifier = Modifier.weight(1f),
        )
        val requestLabel = if (requestCount > 0) "${stringResource(R.string.tab_requests)} ($requestCount)" else stringResource(R.string.tab_requests)
        SegmentChip(
            label = requestLabel,
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
    val bg = if (selected) FzPrimary else FzSurface
    val fg = if (selected) Color.White else FzTextSecondary
    val border = if (selected) FzPrimary else FzBorderGray
    Text(
        text = label,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
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
    onInviteClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                FriendZoneTextField(
                    label = stringResource(R.string.label_add_friend),
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = stringResource(R.string.label_email_username),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                )
                Spacer(modifier = Modifier.height(12.dp))
                FriendZoneOutlineButton(
                    text = stringResource(R.string.header_invite_friends),
                    onClick = onInviteClick,
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
                        Text(stringResource(R.string.msg_no_user_found), color = FzTextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    is LookupResult.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(lookupResult.message, color = FzTextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    null -> Unit
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.label_your_friends), style = MaterialTheme.typography.labelLarge, color = FzTextMain)
                Spacer(modifier = Modifier.height(12.dp))
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
                    CircularProgressIndicator(color = FzPrimary)
                }
            }
        } else if (friends.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.msg_no_friends),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = FzTextSecondary,
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
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface)
            .border(1.dp, FzBorderGray, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserInitialAvatar(displayName = user.displayName)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.displayName, style = MaterialTheme.typography.bodyMedium, color = FzTextMain)
            Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
        }
        if (isSending) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = FzPrimary, strokeWidth = 2.dp)
        } else {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FzPrimary),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Send request", tint = Color.White)
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
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface)
            .border(1.dp, FzBorderGray, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserInitialAvatar(displayName = user.displayName)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(user.displayName, style = MaterialTheme.typography.bodyMedium, color = FzTextMain)
            Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
    ) {
        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = FzPrimary)
                    }
                }
            }
            requests.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(R.string.msg_no_pending_requests), color = FzTextSecondary)
                    }
                }
            }
            else -> {
                items(requests, key = { it.id }) { request ->
                    RequestRow(
                        request = request,
                        onAccept = { onAccept(request.id) },
                        onReject = { onReject(request.id) },
                    )
                }
            }
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface)
            .border(1.dp, FzBorderGray, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserInitialAvatar(displayName = request.requester.displayName)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(request.requester.displayName, style = MaterialTheme.typography.bodyMedium, color = FzTextMain)
            Text("@${request.requester.username}", style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
        }
        Text(
            stringResource(R.string.btn_accept),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(FzPrimary)
                .clickable(onClick = onAccept)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            stringResource(R.string.btn_reject),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, FzBorderGray, RoundedCornerShape(8.dp))
                .clickable(onClick = onReject)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = FzTextSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
