package com.example.prisma.back

import com.google.gson.annotations.SerializedName

data class ReservaRequest(
    @SerializedName("usuarioId") val usuarioId: String,
    @SerializedName("salaId") val salaId: String,
    @SerializedName("dataReserva") val dataReserva: String,
    @SerializedName("horarioInicio") val horarioInicio: String,
    @SerializedName("horarioFim") val horarioFim: String,
    @SerializedName("Motivo") val motivo: String
)

data class ReservaResponse(
    @SerializedName("id", alternate = ["Id"])
    val id: String? = null,

    @SerializedName("usuarioId", alternate = ["UsuarioId", "id_usuario", "Id_usuario", "idUsuario"])
    val usuarioId: String? = null,

    @SerializedName("nomeUsuario", alternate = ["NomeUsuario", "nome", "Nome"])
    val nomeUsuario: String? = null,

    @SerializedName("Motivo", alternate = ["motivo", "descricao", "Descricao"])
    val motivo: String? = null,

    @SerializedName("salaId", alternate = ["SalaId", "id_Sala", "Id_Sala", "idSala"])
    val salaId: String? = null,

    @SerializedName("dataReserva", alternate = ["DataReserva", "data_reserva"])
    val dataReserva: String? = null,

    @SerializedName("horarioInicio", alternate = ["HorarioInicio", "horario_inicio"])
    val horarioInicio: String? = null,

    @SerializedName("horarioFim", alternate = ["HorarioFim", "horario_fim"])
    val horarioFim: String? = null,

    val status: Int? = null,
    val message: String? = null,

    @SerializedName("waitlistId", alternate = ["WaitlistId", "waitlist_id"])
    val waitlistId: Int? = null
)

data class SalaResponse(
    val id: String? = null,
    val nome: String? = null,
    val capacidade: Int? = null,
    val disponibilidade: Boolean? = null
)