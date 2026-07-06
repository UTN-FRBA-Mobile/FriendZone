package com.example.friendzone.presentation.invite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.domain.util.resolveApiAssetUrl
import com.example.friendzone.presentation.components.ProfileIconItem
import com.example.friendzone.presentation.components.ProfileIconStyle
import com.example.friendzone.ui.theme.FzGreen
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingInviteBottomSheet(
    username: String,
    onDismiss: () -> Unit,
    onAdded: () -> Unit,
    viewModel: IncomingInviteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(username) { viewModel.load(username) }
    LaunchedEffect(uiState.added) { if (uiState.added) onAdded() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = FzInk)
                    }
                }

                uiState.isSelf -> {
                    Text(
                        "This is your own invite link",
                        style = MaterialTheme.typography.titleMedium,
                        color = FzInk,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Share it with your friends so they can add you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FzInk3,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    FriendZoneOutlineButton(text = "Close", onClick = onDismiss)
                }

                uiState.inviter == null -> {
                    Text(
                        uiState.errorMessage ?: "We couldn't find this user.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FzInk3,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    FriendZoneOutlineButton(text = "Close", onClick = onDismiss)
                }

                else -> {
                    val inviter = uiState.inviter!!
                    ProfileIconItem(
                        displayName = inviter.displayName,
                        profilePictureUrl = resolveApiAssetUrl(inviter.profilePictureUrl),
                        size = 72.dp,
                        style = ProfileIconStyle.Dark,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        inviter.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = FzInk,
                    )
                    Text(
                        "@${inviter.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FzInk3,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    when {
                        uiState.added -> {
                            Text(
                                "You and ${inviter.displayName} are now friends! 🎉",
                                style = MaterialTheme.typography.titleMedium,
                                color = FzGreen,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            FriendZoneOutlineButton(text = "Close", onClick = onDismiss)
                        }

                        uiState.alreadyFriend -> {
                            Text(
                                "You and ${inviter.displayName} are already friends",
                                style = MaterialTheme.typography.bodyLarge,
                                color = FzInk,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            FriendZoneOutlineButton(text = "Close", onClick = onDismiss)
                        }

                        else -> {
                            Text(
                                "${inviter.displayName} invited you to be their friend",
                                style = MaterialTheme.typography.bodyLarge,
                                color = FzInk,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            FriendZonePrimaryButton(
                                text = "Add",
                                onClick = viewModel::add,
                                isLoading = uiState.isAdding,
                            )
                            uiState.errorMessage?.let { message ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
