package com.example.prisma.back

import com.google.gson.annotations.SerializedName

data class WaitlistResponse(
    @SerializedName("id") val id: String,
    @SerializedName("usuarioId") val usuarioId: String,
    @SerializedName("salaId") val salaId: String,
    @SerializedName("dataReserva") val dataReserva: String,
    @SerializedName("horarioInicio") val horarioInicio: String,
    @SerializedName("horarioFim") val horarioFim: String,
    @SerializedName("dataSolicitacao") val dataSolicitacao: String
)