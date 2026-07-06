package com.example.friendzone.domain.result

import android.content.Context
import com.example.friendzone.R

sealed class AppError {
    data class Http(val code: Int, val message: String) : AppError()
    data object Unauthorized : AppError()
    data object Network : AppError()
    data class Unknown(val message: String) : AppError()
}

fun AppError.displayMessage(context: Context): String = when (this) {
    is AppError.Http -> humanizeHttpError(context, code, message)
    AppError.Unauthorized -> context.getString(R.string.error_session_expired)
    AppError.Network -> context.getString(R.string.error_network)
    is AppError.Unknown -> message.ifBlank { context.getString(R.string.error_unknown) }
}

private fun humanizeHttpError(context: Context, code: Int, rawMessage: String): String {
    val message = rawMessage.trim()
    if (message.isBlank() || isGenericHttpMessage(message)) {
        return defaultHttpMessage(context, code)
    }

    return message
        .split("\n")
        .joinToString("\n") { line -> humanizeApiLine(context, line.trim()) }
        .ifBlank { defaultHttpMessage(context, code) }
}

private fun isGenericHttpMessage(message: String): Boolean {
    val lower = message.lowercase()
    return lower == "bad request" ||
        lower == "conflict" ||
        lower == "unauthorized" ||
        lower.startsWith("http ")
}

private fun humanizeApiLine(context: Context, line: String): String {
    val lower = line.lowercase()
    return when {
        lower == "email already registered" ->
            context.getString(R.string.error_email_registered)
        lower == "username already taken" ->
            context.getString(R.string.error_username_taken)
        lower == "invalid credentials" ->
            context.getString(R.string.error_invalid_credentials)
        lower.contains("email must be an email") ->
            context.getString(R.string.error_invalid_email)
        lower.contains("password must be longer than or equal to 8") ||
            lower.contains("password must be longer than") ->
            context.getString(R.string.error_password_length)
        lower.contains("username must contain only letters, numbers, and underscores") ->
            context.getString(R.string.error_username_format)
        lower.contains("username must be longer than or equal to 3") ->
            context.getString(R.string.error_username_length)
        lower.contains("displayname should not be empty") ||
            lower.contains("displayname must be longer") ->
            context.getString(R.string.error_display_name_required)
        else -> line.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

private fun defaultHttpMessage(context: Context, code: Int): String = when (code) {
    400 -> context.getString(R.string.error_invalid_info)
    401 -> context.getString(R.string.error_session_expired)
    409 -> context.getString(R.string.error_account_exists)
    422 -> context.getString(R.string.error_invalid_info)
    in 500..599 -> context.getString(R.string.error_server)
    else -> context.getString(R.string.error_unknown)
}
