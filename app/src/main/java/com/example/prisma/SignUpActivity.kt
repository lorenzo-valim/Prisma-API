package com.example.prisma

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.prisma.back.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private var passoAtual = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val layoutEtapaDados = findViewById<LinearLayout>(R.id.layoutEtapaDados)
        val layoutEtapaCodigo = findViewById<LinearLayout>(R.id.layoutEtapaCodigo)
        val btnProximo = findViewById<Button>(R.id.btnProximo)
        val tvPasso = findViewById<TextView>(R.id.tvPasso)

        val etNovoNome = findViewById<EditText>(R.id.etNovoNome)
        val etNovoEmail = findViewById<EditText>(R.id.etNovoEmail)
        val etNovaSenha = findViewById<EditText>(R.id.etNovaSenha)
        val etCodigoVerificacao = findViewById<EditText>(R.id.etCodigoVerificacao)

        btnProximo.setOnClickListener {
            if (passoAtual == 1) {
                val nome = etNovoNome.text.toString().trim()
                val email = etNovoEmail.text.toString().trim()
                val senha = etNovaSenha.text.toString().trim()

                if (validarPasso1(nome, email, senha, etNovoNome, etNovoEmail, etNovaSenha)) {

                    val request = RegisterRequest(nome, email, senha, tipo = 1)

                    RetrofitClient.instance.registrar(request).enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                layoutEtapaDados.visibility = View.GONE
                                layoutEtapaCodigo.visibility = View.VISIBLE
                                tvPasso.text = "Passo 2 de 2"
                                btnProximo.text = "Confirmar Código"
                                passoAtual = 2
                            } else {
                                val erro = response.errorBody()?.string() ?: "Erro no cadastro"
                                Toast.makeText(this@SignUpActivity, erro, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Toast.makeText(this@SignUpActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

            } else {
                val codigo = etCodigoVerificacao.text.toString().trim()
                val email = etNovoEmail.text.toString().trim()

                if (codigo.length == 6) {

                    val requestOtp = VerifyOtpRequest(email, codigo)

                    RetrofitClient.instance.verificarOtp(requestOtp).enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@SignUpActivity, "Conta ativada!", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                val erro = response.errorBody()?.string() ?: "Código inválido"
                                etCodigoVerificacao.error = erro
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Toast.makeText(this@SignUpActivity, "Erro de rede", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    etCodigoVerificacao.error = "Digite os 6 dígitos"
                }
            }
        }
    }
    private fun validarPasso1(nome: String, email: String, senha: String, etNome: EditText, etEmail: EditText, etSenha: EditText): Boolean {
        etNome.error = null
        etEmail.error = null
        etSenha.error = null

        return when {
            nome.isEmpty() -> { etNome.error = "Nome obrigatório"; false }
            email.isEmpty() -> { etEmail.error = "E-mail obrigatório"; false }
            senha.length < 6 -> { etSenha.error = "Mínimo 6 caracteres"; false }
            else -> true
        }
    }
}