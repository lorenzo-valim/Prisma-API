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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Calendar

class AgendamentoActivity : AppCompatActivity() {

    private var dataFinalParaConfirmar = ""
    private var horaFinalParaConfirmar = ""

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

        findViewById<TextView>(R.id.tvNomeUsuario).text =
            "Olá, ${intent.getStringExtra("NOME_USUARIO") ?: "Usuário"}"

        iconSair.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnNovoAgendamento.setOnClickListener {
            abrirCalendario(tvResumoTexto, layoutConfirmacao)
        }

        btnConfirmar.setOnClickListener {
            val selectedChipId = chipGroup.checkedChipId
            if (selectedChipId != View.NO_ID) {
                val selectedChip = findViewById<Chip>(selectedChipId)
                val textoDaTag = selectedChip.text.toString()
            } else {
                Toast.makeText(this, "Por favor, selecione uma tag", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirCalendario(tvResumo: TextView, layout: View) {
        val cal = Calendar.getInstance()
        val dpd = DatePickerDialog(this, { _, ano, mes, dia ->
            val calSelecao = Calendar.getInstance()
            calSelecao.set(ano, mes, dia)

            if (calSelecao.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Toast.makeText(this, "Não atendemos aos domingos!", Toast.LENGTH_SHORT).show()
            } else {
                dataFinalParaConfirmar = String.format("%02d/%02d/%d", dia, mes + 1, ano)
                abrirRelogio(tvResumo, layout)
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dpd.datePicker.minDate = System.currentTimeMillis()
        dpd.show()
    }

    private fun abrirRelogio(tvResumo: TextView, layout: View) {
        val cal = Calendar.getInstance()
        val tpd = TimePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            { _, hora, minuto ->

                if (hora in 8..20) {
                    horaFinalParaConfirmar = String.format("%02d:%02d", hora, minuto)
                    tvResumo.text =
                        "📅 Data: $dataFinalParaConfirmar\n⏰ Horário: $horaFinalParaConfirmar"
                    layout.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Escolha entre 08h e 21h", Toast.LENGTH_SHORT).show()
                }
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        )
        tpd.window?.setBackgroundDrawableResource(android.R.color.transparent)
        tpd.show()
    }

    private fun adicionarCardAgendamento(
        container: LinearLayout,
        dataHora: String,
        desc: String,
        tvVazio: TextView
    ) {
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
            text = desc
            setTextColor(Color.parseColor("#757575"))
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
                    .setTitle("Excluir Agendamento")
                    .setMessage("Tem certeza que deseja remover este horário?")
                    .setPositiveButton("Sim") { _, _ ->
                        container.removeView(card)
                        if (container.childCount == 0) tvVazio.visibility = View.VISIBLE
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