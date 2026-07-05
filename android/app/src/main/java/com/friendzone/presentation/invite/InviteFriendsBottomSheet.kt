package com.example.friendzone.presentation.invite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteFriendsBottomSheet(
    onDismiss: () -> Unit,
    viewModel: InviteViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val link = uiState.inviteLink

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                "Invitá a tus amigos",
                style = MaterialTheme.typography.titleMedium,
                color = FzInk,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Compartí tu link personal. Cuando lo abran, la app se abre para agregarte como amigo.",
                style = MaterialTheme.typography.bodySmall,
                color = FzInk3,
            )
            Spacer(modifier = Modifier.height(20.dp))

            when {
                uiState.isLoading && link == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = FzInk)
                    }
                }

                link == null -> {
                    Text(
                        "No pudimos generar tu link. Reintentá más tarde.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FzInk3,
                    )
                }

                else -> {
                    Text("TU LINK", style = MaterialTheme.typography.labelSmall, color = FzInk3)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FzSurface2)
                            .border(1.5.dp, FzBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                    ) {
                        Text(link, style = MaterialTheme.typography.bodyMedium, color = FzInk)
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    FriendZonePrimaryButton(
                        text = "Compartir",
                        onClick = { shareInvite(context, viewModel.shareMessage(link)) },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FriendZoneOutlineButton(
                        text = "Copiar link",
                        onClick = {
                            copyToClipboard(context, link)
                            Toast.makeText(context, "Link copiado", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("FriendZone invite", text))
}

private fun shareInvite(context: Context, message: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Invitar amigos"))
}
