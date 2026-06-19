package com.example.prisma.back

import com.google.gson.annotations.SerializedName
data class ReservaRequest(
    val usuarioId: String,
    val salaId: String,
    val dataReserva: String,
    val horarioInicio: String,
    val horarioFim: String
)
data class ReservaResponse(
    @SerializedName("id", alternate = ["Id"])
    val id: String? = null,

    // Tenta ler todas as variações comuns para o ID do Usuário
    @SerializedName("usuarioId", alternate = ["UsuarioId", "id_usuario", "Id_usuario", "idUsuario"])
    val usuarioId: String? = null,

    // Tenta ler todas as variações para o ID da Sala
    @SerializedName("salaId", alternate = ["SalaId", "id_Sala", "Id_Sala", "idSala"])
    val salaId: String? = null,

    // Tenta ler as variações da Data da Reserva
    @SerializedName("dataReserva", alternate = ["DataReserva", "data_reserva"])
    val dataReserva: String? = null,

    // Tenta ler as variações do Horário de Início
    @SerializedName("horarioInicio", alternate = ["HorarioInicio", "horario_inicio"])
    val horarioInicio: String? = null,

    // Tenta ler as variações do Horário de Término
    @SerializedName("horarioFim", alternate = ["HorarioFim", "horario_fim"])
    val horarioFim: String? = null,

    val status: Int? = null,
    val message: String? = null,

    @SerializedName("waitlistId", alternate = ["WaitlistId", "waitlist_id"])
    val waitlistId: Int? = null
)
data class SalaResponse(
    val id: String,
    val nome: String,
    val capacidade: Int,
    val disponibilidade: Boolean
)