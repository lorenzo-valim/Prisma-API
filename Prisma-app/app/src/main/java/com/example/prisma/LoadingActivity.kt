package com.example.prisma

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val nomeDigitado = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, AgendamentoActivity::class.java)
            intent.putExtra("NOME_USUARIO", nomeDigitado)
            startActivity(intent)
            finish()
        }, 1000)
    }
}