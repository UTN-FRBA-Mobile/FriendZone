package com.example.friendzone.data.remote.api

import com.example.friendzone.data.remote.dto.CreateEventRequest
import com.example.friendzone.data.remote.dto.EventDto
import com.example.friendzone.data.remote.dto.SuccessResponseDto
import com.example.friendzone.data.remote.dto.UpdateEventRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface EventsApi {
    @POST("events")
    suspend fun create(@Body body: CreateEventRequest): EventDto

    @GET("events")
    suspend fun getMine(): List<EventDto>

    @GET("events/{id}")
    suspend fun getById(@Path("id") id: String): EventDto

    @PATCH("events/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateEventRequest): EventDto

    @Multipart
    @POST("events/{id}/cover")
    suspend fun uploadCover(
        @Path("id") id: String,
        @Part cover: MultipartBody.Part,
    ): EventDto

    @DELETE("events/{id}")
    suspend fun delete(@Path("id") id: String): SuccessResponseDto
}
