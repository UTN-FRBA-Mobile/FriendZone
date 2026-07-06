package com.example.friendzone.presentation.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.R
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.InviteFriendChip
import com.example.friendzone.presentation.components.PillBadge
import com.example.friendzone.ui.theme.BorderGray
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteGuestsBottomSheet(
    viewModel: EventDetailViewModel,
    onDismiss: () -> Unit,
) {
    val inviteFriends by viewModel.inviteFriends.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedInviteFriendIds.collectAsStateWithLifecycle()
    val pendingInvites by viewModel.pendingInvites.collectAsStateWithLifecycle()
    val submitState by viewModel.inviteSubmitState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isLoading = submitState is InviteSubmitState.Loading

    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is InviteSubmitState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetInviteSubmitState()
            }
            is InviteSubmitState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetInviteSubmitState()
            }
            else -> Unit
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                stringResource(R.string.header_invite_friends),
                style = MaterialTheme.typography.titleMedium,
                color = TextMain,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.create_invite_hint),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(16.dp))

            when {
                inviteFriends.isEmpty() && pendingInvites.isEmpty() -> {
                    Text(
                        stringResource(R.string.create_no_friends_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
                inviteFriends.isEmpty() -> {
                    Text(
                        stringResource(R.string.msg_all_friends_invited),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
                else -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(inviteFriends, key = { it.id }) { friend ->
                            InviteFriendChip(
                                friend = friend,
                                selected = friend.id in selectedIds,
                                onClick = { viewModel.toggleInviteFriendSelection(friend.id) },
                            )
                        }
                    }
                }
            }

            if (pendingInvites.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.msg_already_invited),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextMain,
                )
                Spacer(modifier = Modifier.height(8.dp))
                pendingInvites.forEach { guest ->
                    HorizontalDivider(color = BorderGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            guest.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMain,
                            modifier = Modifier.weight(1f),
                        )
                        PillBadge(guest.statusLabel, guest.pillVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            FriendZonePrimaryButton(
                text = if (isLoading) stringResource(R.string.btn_sending) else stringResource(R.string.btn_send_invites),
                onClick = { viewModel.sendInvites() },
                enabled = selectedIds.isNotEmpty(),
                isLoading = isLoading,
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
