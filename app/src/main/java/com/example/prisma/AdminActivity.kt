package com.example.prisma

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val container = findViewById<LinearLayout>(R.id.containerAdminHorarios)
        val tvVazio = findViewById<TextView>(R.id.tvAdminListaVazia)
        val iconSair = findViewById<ImageView>(R.id.iconSairAdmin)

        iconSair.setOnClickListener { finish() }

        adicionarCardAdmin(container, "10/05/2024 às 14:00", "Pedro - Podcast", tvVazio)
        adicionarCardAdmin(container, "10/05/2024 às 16:00", "Maria - Estudar", tvVazio)
    }

    private fun adicionarCardAdmin(
        container: LinearLayout,
        dataHora: String,
        info: String,
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
            strokeColor = Color.TRANSPARENT
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
            setTypeface(null, Typeface.BOLD)
            textSize = 16f
        }

        val txtDesc = TextView(this).apply {
            text = info
            setTextColor(Color.GRAY)
            textSize = 14f
        }

        layoutTextos.addView(txtData)
        layoutTextos.addView(txtDesc)


        val btnAprovar = ImageButton(this).apply {


            background = null
            setColorFilter(Color.parseColor("#4CAF50"))
            setPadding(20, 20, 20, 20)

            setOnClickListener {
                txtDesc.text = "✅ APROVADO: $info"
                txtDesc.setTextColor(Color.parseColor("#2E7D32"))
                card.strokeColor = Color.parseColor("#4CAF50")
                card.strokeWidth = 6
                card.setCardBackgroundColor(Color.parseColor("#F1F8E9"))

                card.animate().scaleX(1.03f).scaleY(1.03f).setDuration(100).withEndAction {
                    card.animate().scaleX(1f).scaleY(1f).duration = 100
                }

                this.visibility = View.GONE
                Toast.makeText(context, "Agendamento Confirmado!", Toast.LENGTH_SHORT).show()
            }
        }

        val btnDelete = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            background = null
            setColorFilter(Color.RED)
            setPadding(20, 20, 20, 20)
            setOnClickListener {
                AlertDialog.Builder(this@AdminActivity)
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
        layoutHorizontal.addView(btnAprovar)
        layoutHorizontal.addView(btnDelete)
        card.addView(layoutHorizontal)
        container.addView(card)
        tvVazio.visibility = View.GONE
    }
}