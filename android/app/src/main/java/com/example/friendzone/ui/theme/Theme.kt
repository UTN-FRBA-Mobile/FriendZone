package com.example.friendzone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FriendZoneColorScheme = lightColorScheme(
    primary = FzInk,
    onPrimary = Color.White,
    secondary = FzInk2,
    onSecondary = Color.White,
    background = FzBackground,
    onBackground = FzInk,
    surface = FzSurface,
    onSurface = FzInk,
    surfaceVariant = FzSurface2,
    onSurfaceVariant = FzInk2,
    outline = FzBorder,
    error = FzRequired,
)

@Composable
fun FriendZoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FriendZoneColorScheme,
        typography = FriendZoneTypography,
        shapes = FriendZoneShapes,
        content = content,
    )
}
