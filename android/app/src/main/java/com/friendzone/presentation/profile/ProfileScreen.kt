package com.example.friendzone.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZoneTopBar
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface

@Composable
fun ProfileScreen(
    onNotificationsClick: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var proximityAlerts by rememberSaveable { mutableStateOf(true) }
    val user = uiState.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FzBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        FriendZoneTopBar(
            title = "Profile",
            showNotifications = true,
            notificationBadgeCount = notificationBadgeCount,
            onNotificationsClick = onNotificationsClick,
            showSettings = true,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.isLoading && user == null) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            } else {
                val avatarLetter = user?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(FzInk),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(avatarLetter, style = MaterialTheme.typography.displayLarge, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    user?.displayName ?: "—",
                    style = MaterialTheme.typography.headlineMedium,
                    color = FzInk,
                )
                Text(
                    user?.email ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = FzInk3,
                    modifier = Modifier.padding(top = 2.dp),
                )
                user?.username?.takeIf { it.isNotBlank() }?.let { username ->
                    Text(
                        "@$username",
                        style = MaterialTheme.typography.bodySmall,
                        color = FzInk3,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                StatColumn("Friends", uiState.friendsCount, uiState.isLoading)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(FzBorder),
                )
                StatColumn("Events", uiState.eventsCount, uiState.isLoading)
            }
        }

        HorizontalDivider(color = FzBorder, modifier = Modifier.padding(horizontal = 16.dp))

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Location Sharing",
                style = MaterialTheme.typography.labelLarge,
                color = FzInk,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            SettingToggleRow(
                title = "Share my location",
                subtitle = "Friends can see where you are",
                checked = user?.locationSharingEnabled ?: false,
                enabled = !uiState.isUpdatingLocationSharing && user != null,
                onCheckedChange = viewModel::setLocationSharing,
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingToggleRow(
                title = "Proximity alerts",
                subtitle = "Notify when friends are nearby",
                checked = proximityAlerts,
                enabled = true,
                onCheckedChange = { proximityAlerts = it },
            )
            Spacer(modifier = Modifier.height(16.dp))
            FriendZoneOutlineButton(
                text = if (uiState.isLoggingOut) "Logging out..." else "Log out",
                onClick = viewModel::logout,
            )
            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: Int?, isLoading: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when {
                isLoading && value == null -> "…"
                value != null -> value.toString()
                else -> "—"
            },
            style = MaterialTheme.typography.titleLarge,
            color = FzInk,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = FzInk3)
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .background(FzSurface)
            .border(1.5.dp, FzBorder, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = FzInk)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FzInk3)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = FzInk,
                checkedThumbColor = Color.White,
            ),
        )
    }
}
