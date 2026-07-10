package com.medrem.app.data.remote.api

import com.medrem.app.data.remote.dto.UserDto
import com.medrem.app.data.remote.dto.UserProfileUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

/**
 * User profile API endpoints.
 */
interface UserApi {
    @PUT("api/user/profile")
    suspend fun updateProfile(@Body request: UserProfileUpdateDto): Response<UserDto>
}