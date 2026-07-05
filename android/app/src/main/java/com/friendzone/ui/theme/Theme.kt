package com.example.friendzone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FriendZoneColorScheme = lightColorScheme(
    primary = FzPrimary,
    onPrimary = Color.White,
    secondary = FzPrimaryDark,
    onSecondary = Color.White,
    tertiary = FzSuccess,
    background = FzBackground,
    onBackground = FzTextMain,
    surface = FzSurface,
    onSurface = FzTextMain,
    surfaceVariant = FzPrimaryLight,
    onSurfaceVariant = FzPrimaryDark,
    outline = FzBorderGray,
    error = FzError,
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
