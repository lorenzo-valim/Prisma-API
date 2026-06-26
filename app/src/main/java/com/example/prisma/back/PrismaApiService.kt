package com.example.prisma.back

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface PrismaApiService {

    @POST("api/Auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/Auth/register")
    fun registrar(@Body request: RegisterRequest): Call<ResponseBody>

    @POST("api/Auth/verify-otp")
    fun verificarOtp(@Body request: VerifyOtpRequest): Call<ResponseBody>

    @POST("api/Auth/send-otp-reset-password")
    fun solicitarCodigoReset(@Body request: ForgotPasswordRequest): Call<ResponseBody>

    @POST("api/Auth/reset-password")
    fun resetarSenha(@Body request: ResetPasswordRequest): Call<ResponseBody>

    // --- PADRONIZADO: Rotas de Reserva utilizando o padrão correto do seu backend ---
    @POST("api/Reserva")
    fun criarReserva(@Body request: ReservaRequest): Call<ReservaResponse>

    @GET("api/Reserva")
    fun listarReservas(): Call<List<ReservaResponse>>

    @DELETE("api/Reserva/del/{id}")
    fun deletarReserva(@Path("id") id: String): Call<Void>

    @GET("api/Salas")
    fun listarSalas(): Call<List<SalaResponse>>

    @GET("api/Reserva/horario")
    fun obterHorarioServidor(): Call<HorarioResponse>

    @GET("api/Reserva/waitlist")
    fun listarWaitlist(): Call<List<WaitlistResponse>>
    @DELETE("api/Waitlist/del/{id}")
    fun deletarWaitlist(@Path("id") id: String): Call<WaitlistResponse>

    @GET("api/Usuarios/{id}")
    fun obterUsuarioPorId(@Path("id") id: String): Call<LoginResponse>

    @GET("/api/Reserva")
    fun obterTodasAsReservas(): retrofit2.Call<List<ReservaResponse>>

    @DELETE("/api/Reserva/{id}")
    fun cancelarReserva(@Path("id") id: String): retrofit2.Call<ReservaResponse>
}


object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5263/"

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val instance: PrismaApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(PrismaApiService::class.java)
    }
}