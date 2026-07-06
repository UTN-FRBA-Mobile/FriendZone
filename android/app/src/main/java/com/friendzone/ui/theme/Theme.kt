package com.example.friendzone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FriendZoneColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PrimaryDark,
    onSecondary = Color.White,
    tertiary = Success,
    background = Background,
    onBackground = TextMain,
    surface = Surface,
    onSurface = TextMain,
    surfaceVariant = PrimaryLight,
    onSurfaceVariant = PrimaryDark,
    outline = BorderGray,
    error = ErrorColor,
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
