package com.example.friendzone.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.friendzone.ui.theme.Primary

enum class ProfileIconStyle {
    Default,
    Dark,
    Hero,
}

@Composable
fun ProfileIconItem(
    displayName: String,
    profilePictureUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    style: ProfileIconStyle = ProfileIconStyle.Default,
) {
    if (profilePictureUrl.isNullOrBlank()) {
        InitialAvatarFallback(
            displayName = displayName,
            modifier = modifier,
            size = size,
            style = style,
        )
        return
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        InitialAvatarFallback(
            displayName = displayName,
            modifier = Modifier.fillMaxSize(),
            size = size,
            style = style,
        )
        SubcomposeAsyncImage(
            model = profilePictureUrl,
            contentDescription = displayName,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = {
                SkeletonBox(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                )
            },
            error = {
                InitialAvatarFallback(
                    displayName = displayName,
                    modifier = Modifier.fillMaxSize(),
                    size = size,
                    style = style,
                )
            },
        )
    }
}

@Composable
private fun InitialAvatarFallback(
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp,
    style: ProfileIconStyle,
) {
    when (style) {
        ProfileIconStyle.Default -> UserInitialAvatar(
            displayName = displayName,
            modifier = modifier,
            size = size,
        )
        ProfileIconStyle.Dark -> UserInitialAvatarDark(
            displayName = displayName,
            modifier = modifier,
            size = size,
        )
        ProfileIconStyle.Hero -> HeroInitialAvatar(
            displayName = displayName,
            modifier = modifier,
            size = size,
        )
    }
}

@Composable
private fun HeroInitialAvatar(
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp,
) {
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
        )
    }
}
