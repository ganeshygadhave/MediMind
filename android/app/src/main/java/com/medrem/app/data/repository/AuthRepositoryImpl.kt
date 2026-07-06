package com.medrem.app.data.repository

import com.medrem.app.data.local.TokenManager
import com.medrem.app.data.remote.api.AuthApi
import com.medrem.app.data.remote.dto.*
import com.medrem.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponseDto> {
        return try {
            val response = api.login(LoginRequestDto(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveToken(body.accessToken)
                tokenManager.saveUserInfo(body.user.id, body.user.fullName, body.user.email)
                Result.success(body)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequestDto): Result<AuthResponseDto> {
        return try {
            val response = api.register(request)
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveToken(body.accessToken)
                tokenManager.saveUserInfo(body.user.id, body.user.fullName, body.user.email)
                Result.success(body)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMe(): Result<UserDto> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try { api.logout() } catch (_: Exception) { }
        tokenManager.clearAll()
    }
}
