package com.example.friendzone.presentation.events

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.CreateEventHeader
import com.example.friendzone.presentation.components.FriendRow
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.LiveMapPlaceholder
import com.example.friendzone.presentation.components.PillBadge
import com.example.friendzone.presentation.components.PillVariant
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzGreen
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface

@Composable
fun EventDetailScreen(
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inviteSheetOpen by viewModel.inviteSheetOpen.collectAsStateWithLifecycle()

    if (inviteSheetOpen) {
        InviteGuestsBottomSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.closeInviteSheet() },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FzBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        CreateEventHeader(title = "Event", onBackClick = onBack)

        when (val state = uiState) {
            is EventDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = FzInk)
                }
            }
            is EventDetailUiState.Error -> {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.message, color = FzInk3)
                    TextButton(onClick = { viewModel.loadDetail() }) {
                        Text("Retry", color = FzInk)
                    }
                }
            }
            is EventDetailUiState.Data -> {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        state.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = FzInk,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        state.dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = FzInk3,
                    )
                    if (state.isLive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(FzGreen),
                            )
                            Text(
                                "Live",
                                style = MaterialTheme.typography.labelMedium,
                                color = FzInk,
                            )
                        }
                    }
                    if (state.canInviteGuests) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FriendZoneOutlineButton(
                            text = "Add guests",
                            onClick = { viewModel.openInviteSheet() },
                        )
                        if (state.pendingInviteCount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "${state.pendingInviteCount} pending",
                                style = MaterialTheme.typography.bodySmall,
                                color = FzInk3,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LiveMapPlaceholder()
                    Spacer(modifier = Modifier.height(20.dp))
                    ParticipantSection(state.arrived)
                    Spacer(modifier = Modifier.height(12.dp))
                    ParticipantSection(state.inTransit)
                    Spacer(modifier = Modifier.height(12.dp))
                    ParticipantSection(state.delayed)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ParticipantSection(section: ParticipantSectionUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                section.title,
                style = MaterialTheme.typography.labelLarge,
                color = FzInk,
                modifier = Modifier.weight(1f),
            )
            PillBadge("${section.count}", PillVariant.Light)
        }
        if (section.rows.isEmpty()) {
            Text(
                "None",
                style = MaterialTheme.typography.bodySmall,
                color = FzInk3,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else {
            section.rows.forEach { row ->
                HorizontalDivider(color = FzBorder)
                FriendRow(row)
            }
        }
    }
}
