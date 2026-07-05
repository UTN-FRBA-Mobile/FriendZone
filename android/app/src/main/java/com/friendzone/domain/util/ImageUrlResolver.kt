package com.example.friendzone.domain.util

import com.example.friendzone.BuildConfig

fun resolveApiAssetUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    val base = BuildConfig.API_BASE_URL.trimEnd('/')
    val normalized = if (path.startsWith("/")) path else "/$path"
    return "$base$normalized"
}
