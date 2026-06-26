package com.example.prisma

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.prisma.back.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgendamentoActivity : AppCompatActivity() {

    private var dataFinalParaConfirmar = ""
    private var horaInicioParaConfirmar = ""
    private var horaFimParaConfirmar = ""
    private var listaSalas = listOf<SalaResponse>()
    private var salaSelecionadaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendamento)

        val btnNovoAgendamento = findViewById<Button>(R.id.btnNovoAgendamento)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        val layoutConfirmacao = findViewById<LinearLayout>(R.id.layoutConfirmacao)
        val tvResumoTexto = findViewById<TextView>(R.id.tvResumoTexto)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupDescricao)
        val containerHorarios = findViewById<LinearLayout>(R.id.containerHorarios)
        val tvListaVazia = findViewById<TextView>(R.id.tvListaVazia)
        val iconSair = findViewById<ImageView>(R.id.iconSair)
        val spinnerSalas = findViewById<Spinner>(R.id.spinnerSalas)
        val iconRefresh = findViewById<ImageView>(R.id.iconRefresh)

        layoutConfirmacao.visibility = View.GONE

        findViewById<TextView>(R.id.tvNomeUsuario).text =
            "Olá, ${intent.getStringExtra("NOME_USUARIO") ?: "Usuário"}"

        carregarSalas(spinnerSalas)
        carregarDadosCompletosDoUsuario(containerHorarios, tvListaVazia)

        iconSair.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        iconRefresh.setOnClickListener {
            Toast.makeText(this, "Atualizando agendamentos...", Toast.LENGTH_SHORT).show()

            carregarSalas(spinnerSalas)
            carregarDadosCompletosDoUsuario(containerHorarios, tvListaVazia)
            fecharEResetarPainelAgendamento(layoutConfirmacao, chipGroup)
        }

        //(Calendário -> Relógio Início -> Relógio Fim)
        btnNovoAgendamento.setOnClickListener {
            buscarDataApiEAbrirCalendario(tvResumoTexto, layoutConfirmacao)
        }

        btnConfirmar.setOnClickListener {
            val selectedChipId = chipGroup.checkedChipId

            if (dataFinalParaConfirmar.isEmpty() || horaInicioParaConfirmar.isEmpty() || horaFimParaConfirmar.isEmpty() || salaSelecionadaId.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else if (selectedChipId == View.NO_ID) {
                Toast.makeText(this, "Selecione o motivo do uso", Toast.LENGTH_SHORT).show()
            } else {
                val selectedChip = findViewById<Chip>(selectedChipId)
                val usoSala = selectedChip.text.toString()

                executarReservaNaApi(
                    usoSala,
                    containerHorarios,
                    tvListaVazia,
                    layoutConfirmacao,
                    chipGroup
                )
            }
        }
    }

    // limpa os dados inseridos
    private fun fecharEResetarPainelAgendamento(layout: View, chipGroup: ChipGroup) {
        layout.visibility = View.GONE
        chipGroup.clearCheck()
        dataFinalParaConfirmar = ""
        horaInicioParaConfirmar = ""
        horaFimParaConfirmar = ""
    }

    private fun carregarSalas(spinner: Spinner) {
        RetrofitClient.instance.listarSalas().enqueue(object : Callback<List<SalaResponse>> {
            override fun onResponse(call: Call<List<SalaResponse>>, response: Response<List<SalaResponse>>) {
                if (response.isSuccessful) {
                    listaSalas = response.body() ?: emptyList()
                    if (listaSalas.isNotEmpty()) {
                        salaSelecionadaId = listaSalas[0].id ?: ""
                        val nomes = listaSalas.map { "${it.nome ?: "Sala"} (Cap: ${it.capacidade ?: 0})" }
                        val adapter = ArrayAdapter(this@AgendamentoActivity, android.R.layout.simple_spinner_item, nomes)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter

                        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                                salaSelecionadaId = listaSalas[pos].id ?: ""
                            }
                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<SalaResponse>>, t: Throwable) {}
        })
    }

    private fun carregarDadosCompletosDoUsuario(container: LinearLayout, tvVazio: TextView) {
        val userId = getSharedPreferences("PrismaPrefs", MODE_PRIVATE).getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) return

        container.removeAllViews()
        var totalItensAdicionados = 0

        // Buscar Reservas Normais
        RetrofitClient.instance.listarReservas().enqueue(object : Callback<List<ReservaResponse>> {
            override fun onResponse(call: Call<List<ReservaResponse>>, response: Response<List<ReservaResponse>>) {
                if (response.isSuccessful) {
                    val todasReservas = response.body() ?: emptyList()
                    val minhasReservas = todasReservas.filter { (it.usuarioId ?: "").equals(userId, ignoreCase = true) }

                    minhasReservas.forEach { reserva ->
                        val dataReserva = reserva.dataReserva ?: ""
                        val horarioInicio = reserva.horarioInicio ?: "00:00"
                        val horarioFim = reserva.horarioFim ?: "00:00"
                        val reservaId = reserva.id ?: ""

                        // Captura o motivo real da reserva normal vindo da API
                        val motivoUso = reserva.motivo ?: "Não informado"

                        val dataFormatada = if (dataReserva.contains("T")) dataReserva.split("T")[0] else dataReserva
                        val horarioInfo = "$dataFormatada ($horarioInicio - $horarioFim)"

                        // Passa o motivoUso corretamente para a criação do card
                        adicionarCardAgendamento(container, horarioInfo, "Reserva (Confirmado)", tvVazio, reservaId, motivoUso)
                        totalItensAdicionados++
                    }
                }
                atualizarVisibilidadeTextoVazio(totalItensAdicionados, tvVazio)
            }
            override fun onFailure(call: Call<List<ReservaResponse>>, t: Throwable) {
                Toast.makeText(this@AgendamentoActivity, "Erro ao carregar reservas", Toast.LENGTH_SHORT).show()
            }
        })

        // Buscar waitlist (fila de espera)
        RetrofitClient.instance.listarWaitlist().enqueue(object : Callback<List<WaitlistResponse>> {
            override fun onResponse(call: Call<List<WaitlistResponse>>, responseWaitlist: Response<List<WaitlistResponse>>) {
                if (responseWaitlist.isSuccessful) {
                    val todaWaitlist = responseWaitlist.body() ?: emptyList()
                    val minhaWaitlist = todaWaitlist.filter { (it.usuarioId ?: "").equals(userId, ignoreCase = true) }

                    minhaWaitlist.forEach { itemFila ->
                        val dataReserva = itemFila.dataReserva ?: ""
                        val horarioInicio = itemFila.horarioInicio ?: "00:00"
                        val horarioFim = itemFila.horarioFim ?: "00:00"
                        val waitlistId = itemFila.id?.toString() ?: ""

                        val dataFormatada = if (dataReserva.contains("T")) dataReserva.split("T")[0] else dataReserva
                        val horarioInfo = "$dataFormatada ($horarioInicio - $horarioFim)"

                        // Como combinado, para a fila de espera deixamos o texto padrão por enquanto
                        adicionarCardAgendamento(container, horarioInfo, "Fila de Espera", tvVazio, waitlistId, "")
                        totalItensAdicionados++
                    }
                }
                atualizarVisibilidadeTextoVazio(totalItensAdicionados, tvVazio)
            }
            override fun onFailure(call: Call<List<WaitlistResponse>>, t: Throwable) {}
        })
    }

    private fun atualizarVisibilidadeTextoVazio(total: Int, tvVazio: TextView) {
        if (total == 0) {
            tvVazio.visibility = View.VISIBLE
        } else {
            tvVazio.visibility = View.GONE
        }
    }

    private fun buscarDataApiEAbrirCalendario(tvResumo: TextView, layout: View) {
        RetrofitClient.instance.obterHorarioServidor().enqueue(object : Callback<HorarioResponse> {
            override fun onResponse(call: Call<HorarioResponse>, response: Response<HorarioResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val dataApi = response.body()?.horarioBrasilia ?: ""
                    try {
                        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val date = formato.parse(dataApi)
                        val calServidor = Calendar.getInstance()
                        if (date != null) calServidor.time = date

                        abrirCalendario(tvResumo, layout, calServidor)
                    } catch (e: Exception) {
                        abrirCalendario(tvResumo, layout, Calendar.getInstance())
                    }
                } else {
                    abrirCalendario(tvResumo, layout, Calendar.getInstance())
                }
            }
            override fun onFailure(call: Call<HorarioResponse>, t: Throwable) {
                abrirCalendario(tvResumo, layout, Calendar.getInstance())
            }
        })
    }

    private fun abrirCalendario(tvResumo: TextView, layout: View, dataBase: Calendar) {
        val dpd = DatePickerDialog(this, { _, ano, mes, dia ->
            val calSelecao = Calendar.getInstance()
            calSelecao.set(ano, mes, dia)

            if (calSelecao.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Toast.makeText(this, "Não atendemos aos domingos!", Toast.LENGTH_SHORT).show()
            } else {
                dataFinalParaConfirmar = String.format("%02d/%02d/%d", dia, mes + 1, ano)
                abrirRelogio(true, tvResumo, layout)
            }
        }, dataBase.get(Calendar.YEAR), dataBase.get(Calendar.MONTH), dataBase.get(Calendar.DAY_OF_MONTH))

        dpd.datePicker.minDate = dataBase.timeInMillis
        dpd.show()
    }

    private fun abrirRelogio(isInicio: Boolean, tvResumo: TextView, layout: View) {
        val titulo = if (isInicio) "Horário de Início" else "Horário de Término"

        val tpd = TimePickerDialog(this, { _, hora, minuto ->
            val horaFormatada = String.format("%02d:%02d", hora, minuto)

            if (isInicio) {
                horaInicioParaConfirmar = horaFormatada
                abrirRelogio(false, tvResumo, layout)
            } else {
                horaFimParaConfirmar = horaFormatada
                val h1 = horaInicioParaConfirmar.replace(":", "").toInt()
                val h2 = horaFimParaConfirmar.replace(":", "").toInt()

                if (h2 <= h1) {
                    Toast.makeText(this, "O término deve ser após o início!", Toast.LENGTH_SHORT).show()
                    abrirRelogio(false, tvResumo, layout)
                } else {
                    tvResumo.text = "📅 Data: $dataFinalParaConfirmar\n⏰ $horaInicioParaConfirmar até $horaFimParaConfirmar"
                    layout.visibility = View.VISIBLE
                }
            }
        }, if(isInicio) 8 else 9, 0, true)

        tpd.setTitle(titulo)
        tpd.show()
    }

    private fun executarReservaNaApi(uso: String, container: LinearLayout, tvVazio: TextView, layout: View, chipGroup: ChipGroup) {
        val userId = getSharedPreferences("PrismaPrefs", MODE_PRIVATE).getString("USER_ID", "") ?: ""

        val p = dataFinalParaConfirmar.split("/")
        val dataIso = "${p[2]}-${p[1]}-${p[0]}T00:00:00"

        val request = ReservaRequest(
            usuarioId = userId,
            salaId = salaSelecionadaId,
            dataReserva = dataIso,
            horarioInicio = "$horaInicioParaConfirmar:00",
            horarioFim = "$horaFimParaConfirmar:00",
            motivo = uso
        )

        RetrofitClient.instance.criarReserva(request).enqueue(object : Callback<ReservaResponse> {
            override fun onResponse(call: Call<ReservaResponse>, response: Response<ReservaResponse>) {
                if (response.isSuccessful) {
                    val resCorpo = response.body()
                    val ehWaitlist = resCorpo?.waitlistId != null

                    // REFRESH AUTOMÁTICO
                    carregarDadosCompletosDoUsuario(container, tvVazio)
                    fecharEResetarPainelAgendamento(layout, chipGroup)

                    val msgFeedback = if (ehWaitlist)
                        "Horário ocupado. Adicionado à Fila de Espera!"
                    else
                        "Reserva realizada com sucesso!"

                    Toast.makeText(this@AgendamentoActivity, msgFeedback, Toast.LENGTH_LONG).show()
                } else {
                    // Se a API retornar um erro legível, tratamos aqui e também fechamos o painel por segurança
                    carregarDadosCompletosDoUsuario(container, tvVazio)
                    fecharEResetarPainelAgendamento(layout, chipGroup)
                    Toast.makeText(this@AgendamentoActivity, "Verifique seus agendamentos atualizados.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ReservaResponse>, t: Throwable) {
                carregarDadosCompletosDoUsuario(container, tvVazio)
                fecharEResetarPainelAgendamento(layout, chipGroup)

                Toast.makeText(this@AgendamentoActivity, "Agendamento processado! Atualizando lista...", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun adicionarCardAgendamento(
        container: LinearLayout,
        dataHora: String,
        desc: String,
        tvVazio: TextView,
        idRegistro: String,
        motivoReal: String // Parâmetro adicionado para receber o motivo
    ) {
        val layoutConfirmacao = findViewById<LinearLayout>(R.id.layoutConfirmacao)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupDescricao)

        val card = MaterialCardView(this).apply {
            val params = LinearLayout.LayoutParams(-1, -2)
            params.setMargins(0, 0, 0, 24)
            layoutParams = params
            radius = 32f
            setCardBackgroundColor(Color.WHITE)
            setContentPadding(40, 32, 40, 32)
            cardElevation = 6f
        }

        val layoutHorizontal = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val layoutTextos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        val txtData = TextView(this).apply {
            text = dataHora
            setTextColor(Color.BLACK)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
        }

        val txtDesc = TextView(this).apply {
            // Se for fila de espera e estiver em branco, mostra apenas a descrição.
            // Se for reserva normal, mostra "Reserva (Confirmado) - Motivo: Estudar"
            text = if (motivoReal.isEmpty()) desc else "$desc - $motivoReal"
            setTextColor(if (desc.contains("Fila")) Color.parseColor("#FF9800") else Color.parseColor("#757575"))
            textSize = 13f
        }

        layoutTextos.addView(txtData)
        layoutTextos.addView(txtDesc)

        val btnDelete = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            background = null
            setColorFilter(Color.RED)
            setOnClickListener {
                AlertDialog.Builder(this@AgendamentoActivity)
                    .setTitle("Excluir Solicitação")
                    .setMessage("Tem certeza de que deseja cancelar esta ação?")
                    .setPositiveButton("Sim") { _, _ ->

                        val ehWaitlist = desc.contains("Fila", ignoreCase = true)

                        if (ehWaitlist) {
                            RetrofitClient.instance.deletarWaitlist(idRegistro).enqueue(object : Callback<WaitlistResponse> {
                                override fun onResponse(call: Call<WaitlistResponse>, response: Response<WaitlistResponse>) {
                                    if (response.isSuccessful) {
                                        carregarDadosCompletosDoUsuario(container, tvVazio)
                                        fecharEResetarPainelAgendamento(layoutConfirmacao, chipGroup)
                                        Toast.makeText(this@AgendamentoActivity, "Removido da fila com sucesso!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@AgendamentoActivity, "Erro ao remover da fila no servidor.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<WaitlistResponse>, t: Throwable) {
                                    Toast.makeText(this@AgendamentoActivity, "Sem conexão para remover da fila.", Toast.LENGTH_SHORT).show()
                                }
                            })
                        } else {
                            RetrofitClient.instance.deletarReserva(idRegistro).enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    if (response.isSuccessful) {
                                        carregarDadosCompletosDoUsuario(container, tvVazio)
                                        fecharEResetarPainelAgendamento(layoutConfirmacao, chipGroup)
                                        Toast.makeText(this@AgendamentoActivity, "Reserva cancelada com sucesso!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@AgendamentoActivity, "Erro ao deletar reserva no servidor.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Toast.makeText(this@AgendamentoActivity, "Sem conexão para cancelar reserva.", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }
        }

        layoutHorizontal.addView(layoutTextos)
        layoutHorizontal.addView(btnDelete)
        card.addView(layoutHorizontal)

        container.addView(card, 0)
        tvVazio.visibility = View.GONE
    }
}