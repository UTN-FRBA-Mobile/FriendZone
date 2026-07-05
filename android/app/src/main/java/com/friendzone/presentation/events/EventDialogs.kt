package com.example.friendzone.presentation.events

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.friendzone.R
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzTextSecondary

@Composable
fun ConfirmDeleteEventDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_delete_event)) },
        text = { Text(stringResource(R.string.msg_delete_event_confirm), color = FzTextSecondary) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = FzInk,
                    )
                } else {
                    Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.btn_cancel), color = FzInk)
            }
        },
    )
}

@Composable
fun ConfirmLeaveEventDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_leave_event)) },
        text = { Text(stringResource(R.string.msg_leave_event_confirm)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = FzInk,
                    )
                } else {
                    Text(stringResource(R.string.btn_leave), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.btn_cancel), color = FzInk)
            }
        },
    )
}
