package com.kemarport.hindalco.model

data class LoginResponse(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val isVerified: Boolean,
    val jwtToken: String,
    val refreshToken: String,
    val username: String,
    val mobileNumber: String,
    val message: String,
)