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
import com.example.friendzone.presentation.components.UserInitialAvatarDark
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

                uiState.inviter == null -> {
                    Text(
                        uiState.errorMessage ?: "No encontramos a este usuario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FzInk3,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    FriendZoneOutlineButton(text = "Cerrar", onClick = onDismiss)
                }

                else -> {
                    val inviter = uiState.inviter!!
                    UserInitialAvatarDark(displayName = inviter.displayName, size = 72.dp)
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

                    if (uiState.added) {
                        Text(
                            "¡Ya son amigos! 🎉",
                            style = MaterialTheme.typography.titleMedium,
                            color = FzGreen,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        FriendZoneOutlineButton(text = "Cerrar", onClick = onDismiss)
                    } else {
                        Text(
                            "${inviter.displayName} te invitó a ser su amigo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = FzInk,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        FriendZonePrimaryButton(
                            text = "Agregar",
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
