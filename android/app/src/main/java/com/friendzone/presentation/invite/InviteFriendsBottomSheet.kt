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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.R
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
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
                stringResource(R.string.header_invite_your_friends),
                style = MaterialTheme.typography.titleMedium,
                color = FzTextMain,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.msg_invite_personal_link_desc),
                style = MaterialTheme.typography.bodySmall,
                color = FzTextSecondary,
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
                        CircularProgressIndicator(color = FzPrimary)
                    }
                }

                link == null -> {
                    Text(
                        stringResource(R.string.msg_invite_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = FzTextSecondary,
                    )
                }

                else -> {
                    Text(stringResource(R.string.label_your_link), style = MaterialTheme.typography.labelSmall, color = FzTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FzSurface2)
                            .border(1.dp, FzBorderGray, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                    ) {
                        Text(link, style = MaterialTheme.typography.bodyMedium, color = FzTextMain)
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    FriendZonePrimaryButton(
                        text = stringResource(R.string.btn_share),
                        onClick = { 
                            val message = viewModel.shareMessage(context, link)
                            val chooserTitle = context.getString(R.string.header_invite_friends)
                            shareInvite(context, message, chooserTitle)
                        },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FriendZoneOutlineButton(
                        text = stringResource(R.string.btn_copy_link),
                        onClick = {
                            copyToClipboard(context, link)
                            Toast.makeText(context, context.getString(R.string.msg_link_copied), Toast.LENGTH_SHORT).show()
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

private fun shareInvite(context: Context, message: String, chooserTitle: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
}
