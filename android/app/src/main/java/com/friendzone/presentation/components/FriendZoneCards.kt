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
import androidx.compose.ui.unit.dp
import com.example.friendzone.presentation.events.EventDetailStatusBadge
import com.example.friendzone.presentation.events.EventListItemUi
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzSuccess
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzPrimaryDark
import com.example.friendzone.ui.theme.FzPrimaryLight
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import com.example.friendzone.ui.theme.FzError
import com.example.friendzone.ui.theme.FzSurface
import com.example.friendzone.ui.theme.FzSurface2

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
            .background(FzSurface)
            .border(1.dp, FzBorderGray, RoundedCornerShape(16.dp))
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
                            color = FzTextMain,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(FzSuccess),
                            )
                            Text(
                                "Live",
                                style = MaterialTheme.typography.labelMedium,
                                color = FzSuccess,
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isOrganizer && onDeleteClick != null) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FzError)
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = FzTextSecondary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.dateText, style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PillBadge(item.confirmedText, PillVariant.Green)
                item.onTheWayText?.let { PillBadge(it, PillVariant.Amber) }
            }
        }
        if (item.friendPreviews.isNotEmpty()) {
            HorizontalDivider(color = FzBorderGray)
            item.friendPreviews.forEach { friend ->
                FriendRow(friend)
            }
        }
        Box(modifier = Modifier.padding(14.dp)) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FriendZonePrimaryButton(text = "🗺  View Live Map", onClick = onViewMapClick)
                if (!item.isOrganizer && onLeaveClick != null) {
                    FriendZoneOutlineButton(text = "Leave Event", onClick = onLeaveClick)
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
            .background(FzSurface)
            .border(1.dp, FzBorderGray, RoundedCornerShape(16.dp))
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
                        Text(item.title, style = MaterialTheme.typography.titleMedium, color = FzTextMain)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.timeIcon)
                            Text(item.timeLabel, style = MaterialTheme.typography.labelMedium, color = FzTextSecondary)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isOrganizer && onDeleteClick != null) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FzError)
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = FzTextSecondary,
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
                HorizontalDivider(color = FzBorderGray)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarStack(item.avatars, item.extraCount)
                    if (item.extraCount > 0) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            "and ${item.extraCount} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = FzTextSecondary,
                        )
                    }
                }
            }
            if (!item.isOrganizer && onLeaveClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                FriendZoneOutlineButton(text = "Leave Event", onClick = onLeaveClick)
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
        Text(dateText, style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
        statusBadge?.let { EventListStatusBadge(it) }
    }
}

@Composable
private fun EventListStatusBadge(badge: EventDetailStatusBadge) {
    val (dotColor, label) = when (badge) {
        EventDetailStatusBadge.Completed -> FzTextSecondary to "Completed"
        EventDetailStatusBadge.Cancelled -> FzError to "Cancelled"
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
            color = FzTextSecondary,
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
            .background(FzSurface.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(38.dp)
                .background(FzSurface2, RoundedCornerShape(12.dp)),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FzTextSecondary)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = FzTextMain,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (showMenu) {
            Box {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(FzSurface2, RoundedCornerShape(12.dp)),
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Event options", tint = FzTextSecondary)
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
            Text(stepLabel, style = MaterialTheme.typography.labelMedium, color = FzTextMain)
            Text(stepDescription, style = MaterialTheme.typography.labelMedium, color = FzTextSecondary)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(FzSurface2),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FzPrimary),
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
            .background(FzSurface2)
            .border(1.5.dp, FzBorderGray, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (previewModel != null) {
            coil.compose.AsyncImage(
                model = previewModel,
                contentDescription = "Cover preview",
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
                Text("Upload cover image", style = MaterialTheme.typography.labelMedium, color = FzTextSecondary)
                Text("JPG, PNG up to 20MB", style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
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
            .background(FzSurface2)
            .border(1.5.dp, FzBorderGray, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🗺", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Live map coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = FzTextSecondary,
            )
        }
    }
}
