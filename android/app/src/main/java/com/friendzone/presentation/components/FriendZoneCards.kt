package com.example.friendzone.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.friendzone.R
import com.example.friendzone.presentation.events.EventDetailStatusBadge
import com.example.friendzone.presentation.events.EventListItemUi
import com.example.friendzone.ui.theme.BorderGray
import com.example.friendzone.ui.theme.Success
import com.example.friendzone.ui.theme.Primary
import com.example.friendzone.ui.theme.PrimaryDark
import com.example.friendzone.ui.theme.PrimaryLight
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary
import com.example.friendzone.ui.theme.ErrorColor
import com.example.friendzone.ui.theme.Surface
import com.example.friendzone.ui.theme.Surface2

@Composable
fun EventLiveCard(
    item: EventListItemUi,
    onClick: () -> Unit,
    onViewMapClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onLeaveClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EventCoverAvatar(coverImageUrl = item.coverImageUrl)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextMain,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Success),
                            )
                            Text(
                                stringResource(R.string.msg_live_now),
                                style = MaterialTheme.typography.labelMedium,
                                color = Success,
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isOrganizer && onDeleteClick != null) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = ErrorColor)
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.dateText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PillBadge(item.confirmedText, PillVariant.Green)
                item.onTheWayText?.let { PillBadge(it, PillVariant.Amber) }
            }
        }
        if (item.friendPreviews.isNotEmpty()) {
            HorizontalDivider(color = BorderGray)
            item.friendPreviews.forEach { friend ->
                FriendRow(friend)
            }
        }
        Box(modifier = Modifier.padding(14.dp)) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FriendZonePrimaryButton(text = stringResource(R.string.btn_view_live_map), onClick = onViewMapClick)
                if (!item.isOrganizer && onLeaveClick != null) {
                    FriendZoneOutlineButton(text = stringResource(R.string.btn_leave_event), onClick = onLeaveClick)
                }
            }
        }
    }
}

@Composable
fun EventUpcomingCard(
    item: EventListItemUi,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onLeaveClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EventCoverAvatar(coverImageUrl = item.coverImageUrl)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(item.title, style = MaterialTheme.typography.titleMedium, color = TextMain)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.timeIcon)
                            Text(item.timeLabel, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isOrganizer && onDeleteClick != null) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = ErrorColor)
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            EventListDateRow(dateText = item.dateText, statusBadge = item.statusBadge)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PillBadge(item.confirmedText, PillVariant.Green)
                PillBadge(item.pendingText, PillVariant.Light)
            }
            if (item.avatars.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderGray)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarStack(item.avatars, item.extraCount)
                    if (item.extraCount > 0) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            stringResource(R.string.msg_and_more, item.extraCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
            if (!item.isOrganizer && onLeaveClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                FriendZoneOutlineButton(text = stringResource(R.string.btn_leave_event), onClick = onLeaveClick)
            }
        }
    }
}

@Composable
private fun EventListDateRow(
    dateText: String,
    statusBadge: EventDetailStatusBadge?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(dateText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        statusBadge?.let { EventListStatusBadge(it) }
    }
}

@Composable
private fun EventListStatusBadge(badge: EventDetailStatusBadge) {
    val (dotColor, label) = when (badge) {
        EventDetailStatusBadge.Completed -> TextSecondary to stringResource(R.string.msg_completed)
        EventDetailStatusBadge.Cancelled -> ErrorColor to stringResource(R.string.msg_cancelled)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(50))
                .background(dotColor),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}

@Composable
fun CreateEventHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showMenu: Boolean = false,
    onMenuClick: () -> Unit = {},
    menuExpanded: Boolean = false,
    onMenuDismiss: () -> Unit = {},
    menuContent: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(38.dp)
                .background(Surface2, RoundedCornerShape(12.dp)),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_close), tint = TextSecondary)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextMain,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (showMenu) {
            Box {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Surface2, RoundedCornerShape(12.dp)),
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null, tint = TextSecondary)
                }
                if (menuContent != null) {
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = onMenuDismiss,
                    ) {
                        menuContent()
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.size(38.dp))
        }
    }
}

@Composable
fun StepProgressBar(
    stepLabel: String,
    stepDescription: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stepLabel, style = MaterialTheme.typography.labelMedium, color = TextMain)
            Text(stepDescription, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Surface2),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Primary),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
fun UploadZone(
    modifier: Modifier = Modifier,
    previewModel: Any? = null,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2)
            .border(1.5.dp, BorderGray, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (previewModel != null) {
            coil.compose.AsyncImage(
                model = previewModel,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("🖼", style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.create_upload_cover), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(stringResource(R.string.create_upload_hint), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
fun LiveMapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .border(1.5.dp, BorderGray, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🗺", style = MaterialTheme.typography.headlineLarge)
            Text(
                stringResource(R.string.msg_live_map_coming_soon),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}
