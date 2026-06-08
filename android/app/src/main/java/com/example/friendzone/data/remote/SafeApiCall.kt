package com.example.friendzone.data.remote

import com.example.friendzone.data.remote.dto.ApiErrorDto
import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.AppError
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

private val errorJson = Json { ignoreUnknownKeys = true }

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: HttpException) {
    ApiResult.Error(parseHttpError(e))
} catch (_: IOException) {
    ApiResult.Error(AppError.Network)
} catch (e: Exception) {
    ApiResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
}

fun parseHttpError(exception: HttpException): AppError {
    val body = exception.response()?.errorBody()?.string()
    if (!body.isNullOrBlank()) {
        runCatching {
            val dto = errorJson.decodeFromString<ApiErrorDto>(body)
            if (exception.code() == 401) {
                return AppError.Unauthorized
            }
            return AppError.Http(dto.statusCode, dto.message)
        }
    }
    if (exception.code() == 401) {
        return AppError.Unauthorized
    }
    return AppError.Http(exception.code(), exception.message())
}
