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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.friendzone.ui.theme.FabShape
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk2
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface2

enum class BottomNavTab {
    Events,
    Friends,
    Profile,
}

@Composable
fun FriendZoneTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showMenu: Boolean = false,
    showNotifications: Boolean = false,
    notificationBadgeCount: Int = 0,
    showAdd: Boolean = false,
    showSettings: Boolean = false,
    onMenuClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FzBackground.copy(alpha = 0.88f))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (showMenu) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(38.dp)
                    .background(FzSurface2, RoundedCornerShape(12.dp)),
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = FzInk3)
            }
        } else {
            Spacer(modifier = Modifier.size(38.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = FzInk,
            modifier = Modifier.weight(1f),
            textAlign = if (showSettings) TextAlign.Center else TextAlign.Start,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showNotifications) {
                TopBarIconWithBadge(
                    onClick = onNotificationsClick,
                    badgeCount = notificationBadgeCount,
                    contentDescription = "Notifications",
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = FzInk3)
                }
            }
            if (showAdd) {
                TopBarIconButton(onClick = onAddClick, contentDescription = "Create event") {
                    Icon(Icons.Default.Add, contentDescription = null, tint = FzInk3)
                }
            } else if (showSettings) {
                TopBarIconButton(onClick = onSettingsClick, contentDescription = "Settings") {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = FzInk3)
                }
            }
            if (!showNotifications && !showAdd && !showSettings) {
                Spacer(modifier = Modifier.size(38.dp))
            }
        }
    }
}

@Composable
private fun TopBarIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .background(FzSurface2, RoundedCornerShape(12.dp)),
    ) {
        Box(modifier = Modifier.semantics { this.contentDescription = contentDescription }) {
            content()
        }
    }
}

@Composable
private fun TopBarIconWithBadge(
    onClick: () -> Unit,
    badgeCount: Int,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    Box {
        TopBarIconButton(onClick = onClick, contentDescription = contentDescription, content = content)
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(com.example.friendzone.ui.theme.FzRequired),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun FriendZoneBottomBar(
    selectedTab: BottomNavTab,
    onEventsClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFabClick: () -> Unit,
    pendingFriendsCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            BottomNavItem(
                label = "Events",
                selected = selectedTab == BottomNavTab.Events,
                onClick = onEventsClick,
                icon = { Icon(Icons.Default.Event, contentDescription = null) },
            )
            BottomNavItem(
                label = "Friends",
                selected = selectedTab == BottomNavTab.Friends,
                onClick = onFriendsClick,
                badgeCount = pendingFriendsCount,
                icon = { Icon(Icons.Default.People, contentDescription = null) },
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(FabShape)
                    .background(FzInk)
                    .clickable(onClick = onFabClick)
                    .semantics { contentDescription = "Create event" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
            BottomNavItem(
                label = "Profile",
                selected = selectedTab == BottomNavTab.Profile,
                onClick = onProfileClick,
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    icon: @Composable () -> Unit,
) {
    val color = if (selected) FzInk else FzInk3
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides color,
            ) {
                icon()
            }
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-6).dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(com.example.friendzone.ui.theme.FzRequired),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = FzInk3,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

@Composable
fun PillBadge(
    text: String,
    variant: PillVariant,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, borderColor) = when (variant) {
        PillVariant.Dark, PillVariant.Live -> Triple(FzInk, Color.White, Color.Transparent)
        PillVariant.Light -> Triple(FzSurface2, FzInk3, FzBorder)
        PillVariant.Green -> Triple(
            com.example.friendzone.ui.theme.FzGreenBg,
            com.example.friendzone.ui.theme.FzGreen,
            Color.Transparent,
        )
        PillVariant.Amber -> Triple(
            com.example.friendzone.ui.theme.FzAmberBg,
            com.example.friendzone.ui.theme.FzAmber,
            Color.Transparent,
        )
    }
    Text(
        text = text,
        modifier = modifier
            .background(bg, CircleShape)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.5.dp, borderColor, CircleShape)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = fg,
    )
}

@Composable
fun AvatarStack(
    emojis: List<String>,
    extraCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        emojis.forEachIndexed { index, emoji ->
            Box(
                modifier = Modifier
                    .offset(x = if (index == 0) 0.dp else (-8).dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(FzSurface2)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (extraCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (-8).dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(FzSurface2)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("+$extraCount", style = MaterialTheme.typography.labelMedium, color = FzInk3)
            }
        }
    }
}

@Composable
fun FriendRow(friend: FriendRowUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(FzSurface2),
            contentAlignment = Alignment.Center,
        ) {
            Text(friend.initial, style = MaterialTheme.typography.titleMedium, color = FzInk2)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(friend.name, style = MaterialTheme.typography.bodyMedium, color = FzInk)
            Text(friend.subtitle, style = MaterialTheme.typography.bodySmall, color = FzInk3)
        }
        PillBadge(text = friend.pillText, variant = friend.pillVariant)
    }
}
