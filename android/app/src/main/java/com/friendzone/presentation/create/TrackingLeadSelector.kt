package com.example.friendzone.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk2
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface
import com.example.friendzone.ui.theme.FzSurface2

private data class TrackingPreset(val label: String, val minutes: Int)

private val PRESETS = listOf(
    TrackingPreset("15 minutes", 15),
    TrackingPreset("30 minutes", 30),
    TrackingPreset("1 hour", 60),
    TrackingPreset("2 hours", 120),
)

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
            "Start tracking before event",
            style = MaterialTheme.typography.labelLarge,
            color = FzInk,
        )
        Text(
            "Friends can share location starting this long before the event",
            style = MaterialTheme.typography.bodySmall,
            color = FzInk3,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PRESETS.forEach { preset ->
                TrackingChip(
                    label = preset.label,
                    selected = !isCustom && selectedMinutes == preset.minutes,
                    onClick = { onPresetSelected(preset.minutes) },
                )
            }
            TrackingChip(
                label = if (isCustom) "Custom (${formatDuration(selectedMinutes)})" else "Custom",
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
    val bg = if (selected) FzInk else FzSurface2
    val fg = if (selected) FzSurface else FzInk2
    val border = if (selected) FzInk else FzBorder
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(20.dp))
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
        title = { Text("Custom tracking lead") },
        text = {
            Column {
                Text("Hours: $hours", color = FzInk2)
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = { hours = (hours - 1).coerceAtLeast(0) }) { Text("-") }
                    TextButton(onClick = { hours = (hours + 1).coerceAtMost(23) }) { Text("+") }
                }
                Text("Minutes: $minutes", color = FzInk2)
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
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
