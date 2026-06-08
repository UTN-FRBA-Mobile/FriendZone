package com.example.friendzone.domain.result

sealed class AppError {
    data class Http(val code: Int, val message: String) : AppError()
    data object Unauthorized : AppError()
    data object Network : AppError()
    data class Unknown(val message: String) : AppError()
}

fun AppError.displayMessage(): String = when (this) {
    is AppError.Http -> message
    AppError.Unauthorized -> "Session expired. Please log in again."
    AppError.Network -> "Network error. Check your connection."
    is AppError.Unknown -> message
}
