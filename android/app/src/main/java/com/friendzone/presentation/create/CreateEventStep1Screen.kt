package com.example.friendzone.presentation.create

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.R
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.presentation.components.FriendZonePrimaryButton
import com.example.friendzone.presentation.components.FriendZoneTextField
import com.example.friendzone.presentation.components.LocationPickerDialog
import com.example.friendzone.presentation.components.StepProgressBar
import com.example.friendzone.presentation.components.UploadZone
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzSuccess
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventStep1Screen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)

    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val locationMessage by viewModel.locationMessage.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(locationMessage) {
        locationMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeLocationMessage()
        }
    }

    val initialDateMillis = remember(draft.selectedDate) {
        draft.selectedDate?.toDatePickerUtcMillis()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    val timePickerState = rememberTimePickerState(
        initialHour = draft.selectedTime?.hour ?: 12,
        initialMinute = draft.selectedTime?.minute ?: 0,
        is24Hour = false,
    )

    val pickCoverImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { viewModel.setCoverImage(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FzBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        CreateEventHeader(title = stringResource(R.string.header_create_event), onBackClick = onBack)
        StepProgressBar(
            stepLabel = stringResource(R.string.create_step_label, 1, 2),
            stepDescription = stringResource(R.string.create_step1_desc),
            progress = 0.5f,
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            UploadZone(
                previewModel = draft.coverPreviewUri,
                onClick = { pickCoverImage.launch("image/*") },
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneTextField(
                label = stringResource(R.string.label_event_name),
                value = draft.eventName,
                onValueChange = viewModel::updateEventName,
                placeholder = stringResource(R.string.label_event_name),
                required = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                FriendZoneTextField(
                    label = stringResource(R.string.label_location),
                    value = draft.location,
                    onValueChange = viewModel::updateLocation,
                    placeholder = stringResource(R.string.label_location),
                    required = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { viewModel.geocodeTypedLocation() },
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (draft.latitude != null) FzSuccess else FzPrimary)
                        .clickable { showLocationPicker = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = stringResource(R.string.label_location),
                        tint = Color.White,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                FriendZoneTextField(
                    label = stringResource(R.string.label_date),
                    value = viewModel.formattedDate(),
                    onValueChange = {},
                    placeholder = stringResource(R.string.label_date),
                    required = true,
                    modifier = Modifier.weight(1f),
                    onClick = { showDatePicker = true },
                )
                Spacer(modifier = Modifier.width(8.dp))
                FriendZoneTextField(
                    label = stringResource(R.string.label_time),
                    value = viewModel.formattedTime(),
                    onValueChange = {},
                    placeholder = stringResource(R.string.label_time),
                    required = true,
                    modifier = Modifier.weight(1f),
                    onClick = { showTimePicker = true },
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            TrackingLeadSelector(
                selectedMinutes = draft.trackingLeadMinutes,
                isCustom = draft.isCustomTracking,
                onPresetSelected = viewModel::selectTrackingPreset,
                onCustomSelected = viewModel::selectCustomTracking,
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneTextField(
                label = stringResource(R.string.label_description),
                value = draft.description,
                onValueChange = viewModel::updateDescription,
                placeholder = stringResource(R.string.label_description),
                optionalLabel = stringResource(R.string.label_optional),
                minLines = 3,
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneTextField(
                label = stringResource(R.string.label_guest_limit),
                value = draft.guestLimit,
                onValueChange = viewModel::updateGuestLimit,
                placeholder = stringResource(R.string.label_unlimited),
                optionalLabel = stringResource(R.string.label_optional),
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZonePrimaryButton(
                text = stringResource(R.string.btn_continue),
                onClick = onContinue,
                enabled = viewModel.step1Valid(),
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLocationPicker) {
        LocationPickerDialog(
            initialLatitude = draft.latitude,
            initialLongitude = draft.longitude,
            onConfirm = { latitude, longitude ->
                viewModel.updatePickedLocation(latitude, longitude)
                showLocationPicker = false
            },
            onDismiss = { showLocationPicker = false },
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateDate(millis.toDatePickerLocalDate())
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("OK", color = FzPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.btn_cancel), color = FzTextSecondary)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.label_time)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateTime(
                            LocalTime.of(timePickerState.hour, timePickerState.minute),
                        )
                        showTimePicker = false
                    },
                ) {
                    Text("OK", color = FzPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.btn_cancel), color = FzTextSecondary)
                }
            },
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

private fun LocalDate.toDatePickerUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toDatePickerLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
