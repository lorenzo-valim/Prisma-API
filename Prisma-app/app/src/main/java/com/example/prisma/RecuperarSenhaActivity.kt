package com.example.prisma

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RecuperarSenhaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        val layoutEmail = findViewById<LinearLayout>(R.id.etDigitarEmail)
        val layoutLink = findViewById<LinearLayout>(R.id.etLinkEnviado)
        val etEmail = findViewById<EditText>(R.id.etEmailRecuperar)
        val btnEnviar = findViewById<Button>(R.id.btnEnviarLink)
        val btnVoltar = findViewById<Button>(R.id.btnVoltarParaMain)

        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Digite seu e-mail"
                etEmail.requestFocus()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "E-mail inválido"
                etEmail.requestFocus()
            } else {
                layoutEmail.visibility = View.GONE
                layoutLink.visibility = View.VISIBLE
            }

            btnVoltar.setOnClickListener {
                val intent = Intent(this, RecuperarSenhaActivity::class.java)
                finish()
            }
        }
    }
}