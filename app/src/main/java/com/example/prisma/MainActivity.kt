package com.example.prisma

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etEmail = findViewById<EditText>(R.id.etNomeUsuario)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val tvErroEmail = findViewById<TextView>(R.id.tvErroEmail)
        val tvErroSenha = findViewById<TextView>(R.id.tvErroSenha)
        val botaoEntrar = findViewById<Button>(R.id.botao)
        val tvEsqueciSenha = findViewById<TextView>(R.id.tvEsqueciSenha)

        findViewById<TextView>(R.id.tvIrParaCadastro).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        tvEsqueciSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }

        botaoEntrar.setOnClickListener {
            val emailInserido = etEmail.text.toString().trim()
            val senhaInserida = etSenha.text.toString().trim()

            tvErroEmail.visibility = View.GONE
            tvErroSenha.visibility = View.GONE

            var validado = true

            if (emailInserido.isEmpty()) {
                tvErroEmail.text = "O campo não pode estar vazio"
                tvErroEmail.visibility = View.VISIBLE
                validado = false
            }

            if (senhaInserida.isEmpty()) {
                tvErroSenha.text = "A senha não pode estar vazia"
                tvErroSenha.visibility = View.VISIBLE
                validado = false
            }

            if (validado) {
                if (emailInserido == "pedrogay") {
                    val intent = Intent(this, AdminActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val nomeParaMostrar = if (emailInserido.contains("@")) {
                        emailInserido.split("@")[0].replace(".", " ").replaceFirstChar { it.uppercase() }
                    } else {
                        emailInserido.replaceFirstChar { it.uppercase() }
                    }

                    val intent = Intent(this, LoadingActivity::class.java)
                    intent.putExtra("NOME_USUARIO", nomeParaMostrar)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}