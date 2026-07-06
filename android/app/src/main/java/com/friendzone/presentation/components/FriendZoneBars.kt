package com.example.friendzone.presentation.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.friendzone.R
import com.example.friendzone.presentation.events.UserAvatarUi
import com.example.friendzone.ui.theme.FzBackground
import com.example.friendzone.ui.theme.FzBorderGray
import com.example.friendzone.ui.theme.FzPrimary
import com.example.friendzone.ui.theme.FzPrimaryDark
import com.example.friendzone.ui.theme.FzPrimaryLight
import com.example.friendzone.ui.theme.FzTextMain
import com.example.friendzone.ui.theme.FzTextSecondary
import com.example.friendzone.ui.theme.FzSuccess
import com.example.friendzone.ui.theme.FzPending
import com.example.friendzone.ui.theme.FzError
import com.example.friendzone.ui.theme.FzSurface
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
    showSettings: Boolean = false,
    onMenuClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FzSurface.copy(alpha = 0.95f))
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
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = FzTextSecondary)
            }
        } else {
            TopBarLogo()
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = FzTextMain,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showNotifications) {
                TopBarIconWithBadge(
                    onClick = onNotificationsClick,
                    badgeCount = notificationBadgeCount,
                    contentDescription = "Notifications",
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = FzTextSecondary)
                }
            }
            if (showSettings) {
                TopBarIconButton(onClick = onSettingsClick, contentDescription = "Settings") {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = FzTextSecondary)
                }
            }
            if (!showNotifications && !showSettings) {
                Spacer(modifier = Modifier.size(38.dp))
            }
        }
    }
}

@Composable
private fun TopBarLogo() {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        FzPrimaryDark,
                        Color(0xFF004D45),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(38.dp),
        )
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
                    .background(FzError),
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
    pendingFriendsCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            BottomNavItem(
                label = stringResource(R.string.nav_events),
                selected = selectedTab == BottomNavTab.Events,
                onClick = onEventsClick,
                icon = { Icon(Icons.Default.Event, contentDescription = null) },
            )
            BottomNavItem(
                label = stringResource(R.string.nav_friends),
                selected = selectedTab == BottomNavTab.Friends,
                onClick = onFriendsClick,
                badgeCount = pendingFriendsCount,
                icon = { Icon(Icons.Default.People, contentDescription = null) },
            )
            BottomNavItem(
                label = stringResource(R.string.nav_profile),
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
    val color = if (selected) FzPrimary else FzTextSecondary
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
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
                        .background(FzError),
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
        color = FzTextSecondary,
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
        PillVariant.Dark, PillVariant.Live -> Triple(FzTextMain, Color.White, Color.Transparent)
        PillVariant.Light -> Triple(FzPrimaryLight, FzPrimaryDark, FzBorderGray)
        PillVariant.Green -> Triple(FzPrimaryLight, FzSuccess, Color.Transparent)
        PillVariant.Amber -> Triple(Color(0xFFFEF3C7), FzPending, Color.Transparent)
    }
    Text(
        text = text,
        modifier = modifier
            .background(bg, CircleShape)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.dp, borderColor, CircleShape)
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
    avatars: List<UserAvatarUi>,
    extraCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        avatars.forEachIndexed { index, avatar ->
            Box(
                modifier = Modifier
                    .offset(x = if (index == 0) 0.dp else (-8).dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(FzSurface2)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                ProfileIconItem(
                    displayName = avatar.displayName,
                    profilePictureUrl = avatar.profilePictureUrl,
                    size = 34.dp,
                )
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
                Text("+$extraCount", style = MaterialTheme.typography.labelMedium, color = FzTextSecondary)
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
        ProfileIconItem(
            displayName = friend.name,
            profilePictureUrl = friend.profilePictureUrl,
            size = 40.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(friend.name, style = MaterialTheme.typography.bodyMedium, color = FzTextMain)
            Text(friend.subtitle, style = MaterialTheme.typography.bodySmall, color = FzTextSecondary)
        }
        PillBadge(text = friend.pillText, variant = friend.pillVariant)
    }
}
