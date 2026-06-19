package com.example.prisma.back

import com.google.gson.annotations.SerializedName

data class HorarioResponse(
    @SerializedName("horarioBrasilia")
    val horarioBrasilia: String
)