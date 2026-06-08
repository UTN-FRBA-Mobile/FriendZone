package com.example.friendzone.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

private val errorJson = Json { ignoreUnknownKeys = true }

fun parseApiErrorBody(body: String, fallbackStatusCode: Int): Pair<Int, String>? {
    val root = runCatching { errorJson.parseToJsonElement(body).jsonObject }.getOrNull()
        ?: return null

    val statusCode = root["statusCode"]?.jsonPrimitive?.content?.toIntOrNull() ?: fallbackStatusCode
    val message = extractErrorMessage(root["message"]) ?: return null
    return statusCode to message
}

private fun extractErrorMessage(element: JsonElement?): String? {
    if (element == null) return null
    return when (element) {
        is JsonPrimitive -> element.content
        is JsonArray -> element.mapNotNull { item ->
            (item as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
        }.joinToString("\n").takeIf { it.isNotBlank() }
        else -> null
    }
}

fun parseHttpError(exception: HttpException): com.example.friendzone.domain.result.AppError {
    val body = exception.response()?.errorBody()?.string()
    if (!body.isNullOrBlank()) {
        parseApiErrorBody(body, exception.code())?.let { (statusCode, message) ->
            if (statusCode == 401) {
                return com.example.friendzone.domain.result.AppError.Unauthorized
            }
            return com.example.friendzone.domain.result.AppError.Http(statusCode, message)
        }
    }
    if (exception.code() == 401) {
        return com.example.friendzone.domain.result.AppError.Unauthorized
    }
    return com.example.friendzone.domain.result.AppError.Http(
        exception.code(),
        exception.message().orEmpty(),
    )
}
