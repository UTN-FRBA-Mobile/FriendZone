package com.example.friendzone.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * App Colors unified with res/values/colors.xml nomenclature.
 * Using Fz prefix to avoid conflicts with common Kotlin/Android classes (like Error).
 */

val FzPrimary = Color(0xFF03B598)
val FzPrimaryDark = Color(0xFF00765D)
val FzPrimaryLight = Color(0xFFF0FAF8)

val FzTextMain = Color(0xFF1A1A1A)
val FzTextSecondary = Color(0xFF6B7280)
val FzBorderGray = Color(0xFFE5E7EB)

val FzSuccess = Color(0xFF16A34A)
val FzError = Color(0xFFDC2626)
val FzPending = Color(0xFFD97706)

val FzBackground = Color(0xFFF5F4F0)
val FzSurface = Color(0xFFFFFFFF)
val FzSurface2 = Color(0xFFF0EFE9)

// Aliases matching res/values/colors.xml names (without Fz prefix)
// Note: 'Error' and 'Success' are not used here to avoid common naming conflicts.
val Primary = FzPrimary
val PrimaryDark = FzPrimaryDark
val PrimaryLight = FzPrimaryLight
val TextMain = FzTextMain
val TextSecondary = FzTextSecondary
val BorderGray = FzBorderGray
val Success = FzSuccess
val ErrorColor = FzError
val Pending = FzPending
val Background = FzBackground
val Surface = FzSurface
val Surface2 = FzSurface2

// Backward compatibility mappings
val FzInk = FzTextMain
val FzInk3 = FzTextSecondary
val FzGreen = FzSuccess

