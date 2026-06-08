package com.example.friendzone.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.friendzone.presentation.events.EventListItemUi
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzGreen
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk2
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface
import com.example.friendzone.ui.theme.FzSurface2

@Composable
fun EventLiveCard(
    item: EventListItemUi,
    onViewMapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface)
            .border(2.dp, FzInk, RoundedCornerShape(16.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = FzInk,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = FzInk3,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(RoundedCornerShape(50))
                        .background(FzGreen),
                )
                Text(
                    "Happening Now",
                    style = MaterialTheme.typography.labelMedium,
                    color = FzInk,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.dateText, style = MaterialTheme.typography.bodySmall, color = FzInk3)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PillBadge(item.confirmedText, PillVariant.Green)
                item.onTheWayText?.let { PillBadge(it, PillVariant.Amber) }
            }
        }
        if (item.friendPreviews.isNotEmpty()) {
            HorizontalDivider(color = FzBorder)
            item.friendPreviews.forEach { friend ->
                FriendRow(friend)
                HorizontalDivider(color = FzBorder, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
        Box(modifier = Modifier.padding(14.dp)) {
            FriendZonePrimaryButton(text = "🗺  View Live Map", onClick = onViewMapClick)
        }
    }
}

@Composable
fun EventUpcomingCard(
    item: EventListItemUi,
    onArrowClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = FzInk)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View event",
                    tint = FzInk3,
                    modifier = Modifier.clickable(onClick = onArrowClick),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.timeIcon)
                Text(item.timeLabel, style = MaterialTheme.typography.labelMedium, color = FzInk2)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.dateText, style = MaterialTheme.typography.bodySmall, color = FzInk3)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PillBadge(item.confirmedText, PillVariant.Green)
                PillBadge(item.pendingText, PillVariant.Light)
            }
            if (item.avatars.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = FzBorder)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarStack(item.avatars, item.extraCount)
                    if (item.extraCount > 0) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            "and ${item.extraCount} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = FzInk3,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateEventHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FzSurface.copy(alpha = 0.92f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(38.dp)
                .background(FzSurface2, RoundedCornerShape(12.dp)),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FzInk2)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = FzInk,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(38.dp))
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
            Text(stepLabel, style = MaterialTheme.typography.labelMedium, color = FzInk)
            Text(stepDescription, style = MaterialTheme.typography.labelMedium, color = FzInk3)
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
                    .background(FzInk),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
fun UploadZone(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(FzSurface2)
            .border(1.5.dp, FzBorder, RoundedCornerShape(10.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🖼", style = MaterialTheme.typography.headlineMedium)
        Text("Upload cover image", style = MaterialTheme.typography.labelMedium, color = FzInk2)
        Text("JPG, PNG up to 5MB", style = MaterialTheme.typography.bodySmall, color = FzInk3)
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
            .border(1.5.dp, FzBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🗺", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Live map coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = FzInk3,
            )
        }
    }
}
