package com.example.prisma

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.prisma.back.LoginRequest
import com.example.prisma.back.LoginResponse
import com.example.prisma.back.RetrofitClient

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
            val intent = Intent(this, RecuperarSenhaActivity::class.java)
            startActivity(intent)
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

                val loginRequest = LoginRequest(emailInserido, senhaInserida)

                RetrofitClient.instance.login(loginRequest)
                    .enqueue(object : retrofit2.Callback<LoginResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<LoginResponse>,
                            response: retrofit2.Response<LoginResponse>
                        ) {
                            if (response.isSuccessful) {
                                val loginResponse = response.body()
                                val usuarioId = loginResponse?.id

                                if (usuarioId != null) {
                                    // 1. Salva os dados iniciais que vieram do Login
                                    val sharedPreferences = getSharedPreferences("PrismaPrefs", MODE_PRIVATE)
                                    sharedPreferences.edit()
                                        .putString("USER_ID", usuarioId)
                                        .putString("USER_EMAIL", loginResponse.email)
                                        .apply()

                                    // 2. CHAMADA DEFINITIVA: Bate no banco para buscar o tipo real do usuário
                                    RetrofitClient.instance.obterUsuarioPorId(usuarioId)
                                        .enqueue(object : retrofit2.Callback<LoginResponse> {
                                            override fun onResponse(
                                                call: retrofit2.Call<LoginResponse>,
                                                userResponse: retrofit2.Response<LoginResponse>
                                            ) {
                                                if (userResponse.isSuccessful && userResponse.body() != null) {
                                                    val usuarioCompleto = userResponse.body()!!

                                                    // Salva o tipo numérico real encontrado no MySQL
                                                    sharedPreferences.edit().putInt("USER_TIPO", usuarioCompleto.tipo ?: 1).apply()

                                                    // 3. Validação estrita pelo banco (2 = Admin, qualquer outra coisa = Comum)
                                                    if (usuarioCompleto.tipo == 2) {
                                                        startActivity(Intent(this@MainActivity, AdminActivity::class.java))
                                                    } else {
                                                        val intent = Intent(this@MainActivity, LoadingActivity::class.java)
                                                        intent.putExtra(
                                                            "NOME_USUARIO",
                                                            loginResponse.message.replace("Bem-vindo, ", "").replace("!", "")
                                                        )
                                                        startActivity(intent)
                                                    }
                                                    finish()
                                                } else {
                                                    tvErroEmail.text = "Erro ao validar permissões no banco de dados."
                                                    tvErroEmail.visibility = View.VISIBLE
                                                }
                                            }

                                            override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                                                tvErroEmail.text = "Falha de conexão ao verificar nível de acesso."
                                                tvErroEmail.visibility = View.VISIBLE
                                            }
                                        })
                                } else {
                                    tvErroEmail.text = "Erro interno: ID do usuário não retornado."
                                    tvErroEmail.visibility = View.VISIBLE
                                }

                            } else if (response.code() == 401) {
                                tvErroSenha.text = "E-mail ou senha incorretos"
                                tvErroSenha.visibility = View.VISIBLE
                            } else {
                                tvErroEmail.text = "Erro no servidor. Tente novamente."
                                tvErroEmail.visibility = View.VISIBLE
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                            tvErroEmail.text = "Sem conexão com a internet ou API fora do ar"
                            tvErroEmail.visibility = View.VISIBLE
                        }
                    })
            }
        }
    }
}