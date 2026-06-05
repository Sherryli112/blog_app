package com.funtime.blog.data.api.dto

data class LoginRequestDto(
    val identifier: String,
    val password: String
)

data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val jwt: String,
    val user: UserDto
)

data class UserDto(
    val id: Int,
    val username: String,
    val email: String
)
