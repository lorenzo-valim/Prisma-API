package com.example.prisma.back

    data class RegisterRequest(
        val nome: String,
        val email: String,
        val password: String,
        val tipo: Int = 1
    )

    data class VerifyOtpRequest(
        val email: String,
        val code: String
    )