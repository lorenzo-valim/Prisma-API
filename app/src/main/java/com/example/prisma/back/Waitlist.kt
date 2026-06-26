package com.example.prisma.back

import com.google.gson.annotations.SerializedName

data class WaitlistResponse(
    @SerializedName("id", alternate = ["Id"]) val id: String? = null,

    @SerializedName("usuarioId", alternate = ["UsuarioId"]) val usuarioId: String? = null,

    // Os dois campos novos que vem da sua API
    @SerializedName("nomeUsuario", alternate = ["NomeUsuario"]) val nomeUsuario: String? = null,
    @SerializedName("Motivo", alternate = ["motivo"]) val motivo: String? = null,

    @SerializedName("salaId", alternate = ["SalaId"]) val salaId: String? = null,
    @SerializedName("dataReserva", alternate = ["DataReserva"]) val dataReserva: String? = null,
    @SerializedName("horarioInicio", alternate = ["HorarioInicio"]) val horarioInicio: String? = null,
    @SerializedName("horarioFim", alternate = ["HorarioFim"]) val horarioFim: String? = null,
    @SerializedName("dataSolicitacao", alternate = ["DataSolicitacao"]) val dataSolicitacao: String? = null
)