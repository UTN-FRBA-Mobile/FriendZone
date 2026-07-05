package com.example.friendzone.presentation.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.R
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePullToRefreshBox
import com.example.friendzone.presentation.components.FriendZoneSwitch
import com.example.friendzone.presentation.components.FriendZoneTopBar
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import com.example.friendzone.ui.theme.FzError
import com.example.friendzone.ui.theme.FzSurface

@Composable
fun ProfileScreen(
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val user = uiState.user
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (result.values.any { it }) {
            viewModel.setLocationSharing(true)
        } else {
            viewModel.showLocationPermissionRequired()
        }
    }

    fun handleLocationSharingChange(enabled: Boolean) {
        if (enabled) {
            if (hasLocationPermission(context)) {
                viewModel.setLocationSharing(true)
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }
        } else {
            viewModel.setLocationSharing(false)
            context.revokeLocationPermissionsOnKill()
        }
    }

    FriendZonePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FzBackground)
                .verticalScroll(rememberScrollState()),
        ) {
            FriendZoneTopBar(
                title = stringResource(R.string.header_profile),
                showNotifications = true,
                notificationBadgeCount = notificationBadgeCount,
                onNotificationsClick = onNotificationsClick,
                showSettings = true,
                onSettingsClick = onSettingsClick
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (uiState.isLoading && user == null) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp), color = FzPrimary)
                } else {
                    val avatarLetter = user?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(FzPrimary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(avatarLetter, style = MaterialTheme.typography.displayLarge, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        user?.displayName ?: "—",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FzTextMain,
                    )
                    Text(
                        user?.email ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = FzTextSecondary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    user?.username?.takeIf { it.isNotBlank() }?.let { username ->
                        Text(
                            "@$username",
                            style = MaterialTheme.typography.bodySmall,
                            color = FzTextSecondary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    StatColumn(stringResource(R.string.header_friends), uiState.friendsCount, uiState.isLoading)
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(FzBorderGray),
                    )
                    StatColumn(stringResource(R.string.header_events), uiState.eventsCount, uiState.isLoading)
                }
            }

            HorizontalDivider(color = FzBorderGray, modifier = Modifier.padding(horizontal = 16.dp))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.label_location),
                    style = MaterialTheme.typography.labelLarge,
                    color = FzTextMain,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                SettingToggleRow(
                    title = stringResource(R.string.msg_share_location),
                    subtitle = stringResource(R.string.msg_location_sharing_desc),
                    checked = user?.locationSharingEnabled ?: false,
                    enabled = !uiState.isUpdatingLocationSharing && user != null,
                    onCheckedChange = ::handleLocationSharingChange,
                )

                Spacer(modifier = Modifier.height(24.dp))
                FriendZoneOutlineButton(
                    text = if (uiState.isLoggingOut) "..." else stringResource(R.string.msg_logout),
                    onClick = viewModel::logout,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) }
                )
                uiState.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(message, color = FzError)
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

private fun Context.revokeLocationPermissionsOnKill() {
    revokeSelfPermissionOnKill(Manifest.permission.ACCESS_FINE_LOCATION)
    revokeSelfPermissionOnKill(Manifest.permission.ACCESS_COARSE_LOCATION)
}

@Composable
private fun StatColumn(label: String, value: Int?, isLoading: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when {
                isLoading && value == null -> "…"
                value != null -> value.toString()
                else -> "0"
            },
            style = MaterialTheme.typography.titleLarge,
            color = FzTextMain,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = FzTextSecondary)
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
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface)
            .border(1.dp, FzBorderGray, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = FzTextMain)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
        }
        FriendZoneSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
