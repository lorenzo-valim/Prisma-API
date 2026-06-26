package com.example.prisma

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prisma.back.ReservaResponse
import com.example.prisma.back.RetrofitClient
import com.example.prisma.back.WaitlistResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminActivity : AppCompatActivity() {

    private lateinit var containerAdminHorarios: LinearLayout
    private lateinit var tvAdminListaVazia: TextView
    private lateinit var iconSairAdmin: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        containerAdminHorarios = findViewById(R.id.containerAdminHorarios)
        tvAdminListaVazia = findViewById(R.id.tvAdminListaVazia)
        iconSairAdmin = findViewById(R.id.iconSairAdmin)

        iconSairAdmin.setOnClickListener {
            val sharedPreferences = getSharedPreferences("PrismaPrefs", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        buscarReservasEWaitlistDoBanco()
    }

    private fun buscarReservasEWaitlistDoBanco() {
        containerAdminHorarios.removeAllViews()
        containerAdminHorarios.addView(tvAdminListaVazia)

        RetrofitClient.instance.obterTodasAsReservas().enqueue(object : Callback<List<ReservaResponse>> {
            override fun onResponse(call: Call<List<ReservaResponse>>, response: Response<List<ReservaResponse>>) {
                val reservas = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()

                RetrofitClient.instance.listarWaitlist().enqueue(object : Callback<List<WaitlistResponse>> {
                    override fun onResponse(call: Call<List<WaitlistResponse>>, responseWaitlist: Response<List<WaitlistResponse>>) {
                        val waitlist = if (responseWaitlist.isSuccessful) responseWaitlist.body() ?: emptyList() else emptyList()

                        val waitlistMapeadaParaReserva = waitlist.map { waitlistItem ->
                            ReservaResponse(
                                id = waitlistItem.id,
                                usuarioId = waitlistItem.usuarioId,
                                nomeUsuario = waitlistItem.nomeUsuario, // Pega o nome que agora vem do C#
                                motivo = waitlistItem.motivo,           // Pega o motivo que agora vem do C#
                                salaId = waitlistItem.salaId,
                                dataReserva = waitlistItem.dataReserva,
                                horarioInicio = waitlistItem.horarioInicio,
                                horarioFim = waitlistItem.horarioFim,
                                status = 99 // F
                            )
                        }

                        // Junta tudo em uma única lista e manda atualizar a tela
                        val listaCompleta = reservas + waitlistMapeadaParaReserva
                        atualizarListaNaTela(listaCompleta)
                    }

                    override fun onFailure(call: Call<List<WaitlistResponse>>, t: Throwable) {
                        atualizarListaNaTela(reservas) // Se a waitlist falhar, mostra pelo menos as reservas
                    }
                })
            }

            override fun onFailure(call: Call<List<ReservaResponse>>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "API fora do ar ou sem internet", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun atualizarListaNaTela(lista: List<ReservaResponse>) {
        containerAdminHorarios.removeAllViews()
        containerAdminHorarios.addView(tvAdminListaVazia)

        if (lista.isEmpty()) {
            tvAdminListaVazia.visibility = View.VISIBLE
        } else {
            tvAdminListaVazia.visibility = View.GONE
            val inflater = LayoutInflater.from(this)

            for (reserva in lista) {
                val cardView: View = inflater.inflate(R.layout.activity_item_admin, containerAdminHorarios, false)

                val tvNome = cardView.findViewById<TextView>(R.id.tvAdminNomeUsuario)
                val tvDataHora = cardView.findViewById<TextView>(R.id.tvAdminDataHora)
                val tvStatus = cardView.findViewById<TextView>(R.id.tvAdminStatus)
                val btnCancelar = cardView.findViewById<Button>(R.id.btnAdminRecusar)
                val btnAprovar = cardView.findViewById<Button>(R.id.btnAdminAprovar)

                btnAprovar.visibility = View.GONE

                val usuarioIdSeguro = reserva.usuarioId?.take(8) ?: "Desconhecido"
                val dataOriginal = reserva.dataReserva ?: "-----T--"
                val dataFormatada = dataOriginal.split("T")[0]
                val horaInicio = reserva.horarioInicio ?: "--:--"
                val horaFim = reserva.horarioFim ?: "--:--"
                val motivoUso = reserva.motivo ?: "Não informado"

                // Usamos apenas o exibicaoUsuario, pois o objeto 'reserva' já carrega
                // os dados da fila de espera se o status for 99.
                val exibicaoUsuario = reserva.nomeUsuario ?: "ID: $usuarioIdSeguro..."

                tvNome.text = "Usuário: $exibicaoUsuario\nMotivo: $motivoUso"
                tvDataHora.text = "Data: $dataFormatada | $horaInicio - $horaFim"

                if (reserva.status == 99) {
                    tvStatus.text = "Status: Fila de Espera"
                    tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                    btnCancelar.text = "Remover da Fila"
                    btnCancelar.visibility = View.VISIBLE
                } else if (reserva.status == 0 || reserva.status == null) {
                    tvStatus.text = "Status: Pendente / Ativa"
                    tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_green_dark))
                    btnCancelar.text = "Recusar"
                    btnCancelar.visibility = View.VISIBLE
                } else {
                    tvStatus.text = "Status: Cancelada"
                    tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_red_dark))
                    btnCancelar.visibility = View.GONE
                }

                btnCancelar.setOnClickListener {
                    reserva.id?.let { idReal ->
                        cardView.animate().alpha(0.3f).setDuration(300).start()
                        btnCancelar.isEnabled = false
                        btnCancelar.text = "Excluindo..."

                        if (reserva.status == 99) {
                            removerWaitlistDoBanco(idReal, cardView)
                        } else {
                            removerReservaDoBanco(idReal, cardView)
                        }
                    } ?: Toast.makeText(this@AdminActivity, "Erro: ID inválido", Toast.LENGTH_SHORT).show()
                }

                containerAdminHorarios.addView(cardView)
            }
        }
    }

    private fun removerReservaDoBanco(idReserva: String, cardParaRemover: View) {
        RetrofitClient.instance.deletarReserva(idReserva).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    containerAdminHorarios.removeView(cardParaRemover)
                    Toast.makeText(this@AdminActivity, "Reserva recusada com sucesso!", Toast.LENGTH_SHORT).show()
                    if (containerAdminHorarios.childCount <= 1) buscarReservasEWaitlistDoBanco()
                } else {
                    restaurarCardErro(cardParaRemover)
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                restaurarCardErro(cardParaRemover)
            }
        })
    }

    private fun removerWaitlistDoBanco(idWaitlist: String, cardParaRemover: View) {
        RetrofitClient.instance.deletarWaitlist(idWaitlist).enqueue(object : Callback<com.example.prisma.back.WaitlistResponse> {
            override fun onResponse(call: Call<com.example.prisma.back.WaitlistResponse>, response: Response<com.example.prisma.back.WaitlistResponse>) {
                if (response.isSuccessful) {
                    containerAdminHorarios.removeView(cardParaRemover)
                    Toast.makeText(this@AdminActivity, "Removido da fila com sucesso!", Toast.LENGTH_SHORT).show()
                    if (containerAdminHorarios.childCount <= 1) buscarReservasEWaitlistDoBanco()
                } else {
                    restaurarCardErro(cardParaRemover, "Remover da Fila")
                }

            }
            override fun onFailure(call: Call<com.example.prisma.back.WaitlistResponse>, t: Throwable) {
                restaurarCardErro(cardParaRemover, "Remover da Fila")
            }
        })
    }

    private fun restaurarCardErro(cardParaRemover: View, textoBotao: String = "Recusar") {
        cardParaRemover.animate().alpha(1.0f).setDuration(200).start()
        val btn = cardParaRemover.findViewById<Button>(R.id.btnAdminRecusar)
        btn.isEnabled = true
        btn.text = textoBotao
        Toast.makeText(this@AdminActivity, "Erro ao processar ação no servidor", Toast.LENGTH_SHORT).show()
    }
}