package com.medrem.app.data.remote.api

import com.medrem.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Auth API endpoints.
 */
interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponseDto>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserDto>
}
