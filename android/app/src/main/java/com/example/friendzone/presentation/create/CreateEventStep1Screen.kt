package com.example.friendzone.presentation.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZoneTextField
import com.example.friendzone.presentation.components.StepProgressBar
import com.example.friendzone.presentation.components.UploadZone
import com.example.friendzone.ui.theme.FzBackground
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventStep1Screen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)

    val draft by viewModel.draft.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val initialDateMillis = remember(draft.selectedDate) {
        draft.selectedDate
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    val timePickerState = rememberTimePickerState(
        initialHour = draft.selectedTime?.hour ?: 12,
        initialMinute = draft.selectedTime?.minute ?: 0,
        is24Hour = false,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FzBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        CreateEventHeader(title = "Create Event", onBackClick = onBack)
        StepProgressBar(
            stepLabel = "Step 1 of 2",
            stepDescription = "Event Details",
            progress = 0.5f,
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            UploadZone()
            Spacer(modifier = Modifier.height(12.dp))
            FriendZoneTextField(
                label = "Event Name",
                value = draft.eventName,
                onValueChange = viewModel::updateEventName,
                placeholder = "Enter event name",
                required = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FriendZoneTextField(
                label = "Location",
                value = draft.location,
                onValueChange = viewModel::updateLocation,
                placeholder = "Enter venue or address",
                required = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                FriendZoneTextField(
                    label = "Date",
                    value = viewModel.formattedDate(),
                    onValueChange = {},
                    placeholder = "Select date",
                    required = true,
                    modifier = Modifier.weight(1f),
                    onClick = { showDatePicker = true },
                )
                Spacer(modifier = Modifier.width(8.dp))
                FriendZoneTextField(
                    label = "Time",
                    value = viewModel.formattedTime(),
                    onValueChange = {},
                    placeholder = "Select time",
                    required = true,
                    modifier = Modifier.weight(1f),
                    onClick = { showTimePicker = true },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TrackingLeadSelector(
                selectedMinutes = draft.trackingLeadMinutes,
                isCustom = draft.isCustomTracking,
                onPresetSelected = viewModel::selectTrackingPreset,
                onCustomSelected = viewModel::selectCustomTracking,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FriendZoneTextField(
                label = "Description",
                value = draft.description,
                onValueChange = viewModel::updateDescription,
                placeholder = "Tell people more about your event...",
                optionalLabel = "(Optional)",
                minLines = 3,
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FriendZoneTextField(
                label = "Guest Limit",
                value = draft.guestLimit,
                onValueChange = viewModel::updateGuestLimit,
                placeholder = "Unlimited",
                optionalLabel = "(Optional)",
            )
            Spacer(modifier = Modifier.height(8.dp))
            FriendZonePrimaryButton(
                text = "Continue →",
                onClick = onContinue,
                enabled = viewModel.step1Valid(),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.updateDate(date)
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select time") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateTime(
                            LocalTime.of(timePickerState.hour, timePickerState.minute),
                        )
                        showTimePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
