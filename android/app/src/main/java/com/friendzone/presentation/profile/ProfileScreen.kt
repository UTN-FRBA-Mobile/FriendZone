package com.example.friendzone.presentation.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.friendzone.domain.util.resolveApiAssetUrl
import com.example.friendzone.presentation.components.FriendZoneOutlineButton
import com.example.friendzone.presentation.components.FriendZonePullToRefreshBox
import com.example.friendzone.presentation.components.FriendZoneSwitch
import com.example.friendzone.presentation.components.FriendZoneTopBar
import com.example.friendzone.presentation.components.ProfileIconItem
import com.example.friendzone.presentation.components.ProfileIconStyle
import com.example.friendzone.ui.theme.Background
import com.example.friendzone.ui.theme.BorderGray
import com.example.friendzone.ui.theme.Primary
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary
import com.example.friendzone.ui.theme.ErrorColor
import com.example.friendzone.ui.theme.Surface
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    var showPictureSheet by remember { mutableStateOf(false) }
    val pictureSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (result.values.any { it }) {
            viewModel.setLocationSharing(true)
        } else {
            viewModel.showLocationPermissionRequired()
        }
    }

    val pickProfilePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            handleProfilePictureSelection(
                context = context,
                uri = it,
                onValid = { bytes, mimeType ->
                    showPictureSheet = false
                    viewModel.uploadProfilePicture(bytes, mimeType)
                },
                onError = { message ->
                    showPictureSheet = false
                    viewModel.showPictureError(message)
                },
            )
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
                .background(Background)
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
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp), color = Primary)
                } else {
                    val isPictureBusy = uiState.isUploadingPicture || uiState.isRemovingPicture
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(enabled = user != null && !isPictureBusy) {
                                showPictureSheet = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        ProfileIconItem(
                            displayName = user?.displayName ?: "?",
                            profilePictureUrl = resolveApiAssetUrl(user?.profilePictureUrl),
                            size = 80.dp,
                            style = ProfileIconStyle.Hero,
                        )
                        if (user != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Primary)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.btn_edit),
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                        if (isPictureBusy) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        user?.displayName ?: "—",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextMain,
                    )
                    Text(
                        user?.email ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    user?.username?.takeIf { it.isNotBlank() }?.let { username ->
                        Text(
                            "@$username",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
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
                            .background(BorderGray),
                    )
                    StatColumn(stringResource(R.string.header_events), uiState.eventsCount, uiState.isLoading)
                }
            }

            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(horizontal = 16.dp))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.label_location),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextMain,
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
                    Text(message, color = ErrorColor)
                }
            }
        }
    }

    if (showPictureSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPictureSheet = false },
            sheetState = pictureSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
            ) {
                Text(
                    stringResource(R.string.profile_picture_choose),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            scope.launch {
                                pictureSheetState.hide()
                                showPictureSheet = false
                            }
                            pickProfilePicture.launch("image/*")
                        }
                        .padding(vertical = 14.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMain,
                )
                if (!user?.profilePictureUrl.isNullOrBlank()) {
                    Text(
                        stringResource(R.string.profile_picture_remove),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch {
                                    pictureSheetState.hide()
                                    showPictureSheet = false
                                }
                                viewModel.removeProfilePicture()
                            }
                            .padding(vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ErrorColor,
                    )
                }
            }
        }
    }
}

private fun handleProfilePictureSelection(
    context: Context,
    uri: Uri,
    onValid: (ByteArray, String) -> Unit,
    onError: (String) -> Unit,
) {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(uri)
    if (mimeType != "image/jpeg" && mimeType != "image/png") {
        onError(context.getString(R.string.profile_picture_invalid_type))
        return
    }
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
    if (bytes == null) {
        return
    }
    if (bytes.size > 20 * 1024 * 1024) {
        onError(context.getString(R.string.profile_picture_too_large))
        return
    }
    onValid(bytes, mimeType)
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
            color = TextMain,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
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
            .background(Surface)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextMain)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        FriendZoneSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
