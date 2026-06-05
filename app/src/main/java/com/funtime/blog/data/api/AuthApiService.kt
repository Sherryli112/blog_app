package com.funtime.blog.data.api

import com.funtime.blog.data.api.dto.AuthResponseDto
import com.funtime.blog.data.api.dto.LoginRequestDto
import com.funtime.blog.data.api.dto.RegisterRequestDto
import com.funtime.blog.data.api.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/local")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/local/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @GET("users/me")
    suspend fun getMe(@Header("Authorization") token: String): UserDto
}
