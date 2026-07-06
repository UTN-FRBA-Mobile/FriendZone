package com.example.friendzone.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.friendzone.ui.theme.TextMain
import com.example.friendzone.ui.theme.TextSecondary
import com.example.friendzone.ui.theme.Surface2

@Composable
fun UserInitialAvatar(
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = Surface2,
    textColor: Color = TextSecondary,
) {
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
        )
    }
}

@Composable
fun UserInitialAvatarDark(
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    UserInitialAvatar(
        displayName = displayName,
        modifier = modifier,
        size = size,
        backgroundColor = TextMain,
        textColor = Color.White,
    )
}
