package com.example.prisma.back
import com.google.gson.annotations.SerializedName
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val id: String,
    val email: String,

    @SerializedName("tipo", alternate = ["Tipo", "role", "Role", "userType"])
    val tipo: Int? = null
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)