package com.example.friendzone.domain.result

sealed class AppError {
    data class Http(val code: Int, val message: String) : AppError()
    data object Unauthorized : AppError()
    data object Network : AppError()
    data class Unknown(val message: String) : AppError()
}

fun AppError.displayMessage(): String = when (this) {
    is AppError.Http -> humanizeHttpError(code, message)
    AppError.Unauthorized -> "Your session has expired. Please sign in again."
    AppError.Network -> "Unable to connect. Check your internet connection and try again."
    is AppError.Unknown -> message.ifBlank { "Something went wrong. Please try again." }
}

private fun humanizeHttpError(code: Int, rawMessage: String): String {
    val message = rawMessage.trim()
    if (message.isBlank() || isGenericHttpMessage(message)) {
        return defaultHttpMessage(code)
    }

    return message
        .split("\n")
        .joinToString("\n") { line -> humanizeApiLine(line.trim()) }
        .ifBlank { defaultHttpMessage(code) }
}

private fun isGenericHttpMessage(message: String): Boolean {
    val lower = message.lowercase()
    return lower == "bad request" ||
        lower == "conflict" ||
        lower == "unauthorized" ||
        lower.startsWith("http ")
}

private fun humanizeApiLine(line: String): String {
    val lower = line.lowercase()
    return when {
        lower == "email already registered" ->
            "This email is already registered. Try signing in instead."
        lower == "username already taken" ->
            "That username is already taken. Please choose another one."
        lower == "invalid credentials" ->
            "Email/username or password is incorrect."
        lower.contains("email must be an email") ->
            "Please enter a valid email address."
        lower.contains("password must be longer than or equal to 8") ||
            lower.contains("password must be longer than") ->
            "Password must be at least 8 characters."
        lower.contains("username must contain only letters, numbers, and underscores") ->
            "Username can only contain letters, numbers, and underscores."
        lower.contains("username must be longer than or equal to 3") ->
            "Username must be at least 3 characters."
        lower.contains("displayname should not be empty") ||
            lower.contains("displayname must be longer") ->
            "Please enter your display name."
        else -> line.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

private fun defaultHttpMessage(code: Int): String = when (code) {
    400 -> "Some of the information you entered is invalid. Please review the form and try again."
    401 -> "Your session has expired. Please sign in again."
    409 -> "An account with this email or username already exists."
    422 -> "Some of the information you entered is invalid. Please review the form and try again."
    in 500..599 -> "Something went wrong on our end. Please try again in a moment."
    else -> "Something went wrong. Please try again."
}
