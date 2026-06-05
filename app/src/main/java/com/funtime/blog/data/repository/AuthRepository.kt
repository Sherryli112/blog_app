package com.funtime.blog.data.repository

import com.funtime.blog.data.api.AuthApiService
import com.funtime.blog.data.api.dto.LoginRequestDto
import com.funtime.blog.data.api.dto.RegisterRequestDto
import com.funtime.blog.data.local.UserSession
import com.funtime.blog.data.local.UserSessionDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionDataStore: UserSessionDataStore
) {
    val sessionFlow: Flow<UserSession?> = sessionDataStore.sessionFlow

    suspend fun login(email: String, password: String): Result<UserSession> = runCatching {
        val response = apiService.login(LoginRequestDto(identifier = email, password = password))
        sessionDataStore.save(response.jwt, response.user)
        UserSession(
            jwt = response.jwt,
            userId = response.user.id,
            username = response.user.username,
            email = response.user.email
        )
    }

    suspend fun register(username: String, email: String, password: String): Result<UserSession> = runCatching {
        val response = apiService.register(RegisterRequestDto(username = username, email = email, password = password))
        sessionDataStore.save(response.jwt, response.user)
        UserSession(
            jwt = response.jwt,
            userId = response.user.id,
            username = response.user.username,
            email = response.user.email
        )
    }

    suspend fun logout() {
        sessionDataStore.clear()
    }
}
