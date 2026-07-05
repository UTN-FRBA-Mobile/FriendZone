package com.example.friendzone.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.friendzone.R
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzPrimaryLight
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import com.example.friendzone.ui.theme.FzSurface
import com.example.friendzone.ui.theme.FzSurface2

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackingLeadSelector(
    selectedMinutes: Int,
    isCustom: Boolean,
    onPresetSelected: (Int) -> Unit,
    onCustomSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            stringResource(R.string.create_tracking_title),
            style = MaterialTheme.typography.labelLarge,
            color = FzTextMain,
        )
        Text(
            stringResource(R.string.create_tracking_desc),
            style = MaterialTheme.typography.bodySmall,
            color = FzTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(15, 30, 60, 120).forEach { mins ->
                val label = when (mins) {
                    60 -> stringResource(R.string.create_preset_hour)
                    120 -> stringResource(R.string.create_preset_hours)
                    else -> stringResource(R.string.create_preset_minutes, mins)
                }
                TrackingChip(
                    label = label,
                    selected = !isCustom && selectedMinutes == mins,
                    onClick = { onPresetSelected(mins) },
                )
            }
            TrackingChip(
                label = if (isCustom) "${stringResource(R.string.create_custom_tracking)} (${formatDuration(selectedMinutes)})" else stringResource(R.string.create_custom_tracking),
                selected = isCustom,
                onClick = { showCustomDialog = true },
            )
        }
    }

    if (showCustomDialog) {
        CustomDurationDialog(
            initialMinutes = selectedMinutes,
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes ->
                onCustomSelected(minutes)
                showCustomDialog = false
            },
        )
    }
}

@Composable
private fun TrackingChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) FzPrimary else FzSurface
    val fg = if (selected) androidx.compose.ui.graphics.Color.White else FzTextSecondary
    val border = if (selected) FzPrimary else com.example.friendzone.ui.theme.FzBorderGray
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        color = fg,
    )
}

@Composable
private fun CustomDurationDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var hours by remember { mutableIntStateOf(initialMinutes / 60) }
    var minutes by remember { mutableIntStateOf(initialMinutes % 60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_custom_tracking_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.create_hours_label, hours), color = FzTextMain)
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = { hours = (hours - 1).coerceAtLeast(0) }) { Text("-") }
                    TextButton(onClick = { hours = (hours + 1).coerceAtMost(23) }) { Text("+") }
                }
                Text(stringResource(R.string.create_minutes_label, minutes), color = FzTextMain)
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = { minutes = (minutes - 5).coerceAtLeast(0) }) { Text("-5") }
                    TextButton(onClick = { minutes = (minutes + 5).coerceAtMost(55) }) { Text("+5") }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val total = (hours * 60 + minutes).coerceIn(1, 1440)
                    onConfirm(total)
                },
            ) {
                Text("OK", color = FzPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = FzTextSecondary) }
        },
    )
}

private fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}
