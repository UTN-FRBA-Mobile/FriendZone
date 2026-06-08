package com.example.friendzone.data.remote

import com.example.friendzone.domain.result.ApiResult
import com.example.friendzone.domain.result.AppError
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: HttpException) {
    ApiResult.Error(parseHttpError(e))
} catch (_: IOException) {
    ApiResult.Error(AppError.Network)
} catch (e: Exception) {
    ApiResult.Error(AppError.Unknown(e.message ?: "Unexpected error"))
}
