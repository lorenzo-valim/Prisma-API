package com.example.prisma

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.prisma.back.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecuperarSenhaActivity : AppCompatActivity() {

    private var emailUsuario: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        val layoutEtapaEmail = findViewById<LinearLayout>(R.id.layoutEtapaEmail)
        val layoutEtapaCodigo = findViewById<LinearLayout>(R.id.layoutEtapaCodigo)
        val layoutEtapaNovaSenha = findViewById<LinearLayout>(R.id.layoutEtapaNovaSenha)

        val etEmail = findViewById<EditText>(R.id.etEmailRecuperar)
        val etCodigo = findViewById<EditText>(R.id.etCodigoOtp)
        val etSenha = findViewById<EditText>(R.id.etNovaSenhaRecuperar)
        val etConfirmar = findViewById<EditText>(R.id.etConfirmarNovaSenha)

        findViewById<Button>(R.id.btnEnviarCodigo).setOnClickListener {
            emailUsuario = etEmail.text.toString().trim()

            if (emailUsuario.isNotEmpty()) {
                val request = ForgotPasswordRequest(emailUsuario)

                RetrofitClient.instance.solicitarCodigoReset(request).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RecuperarSenhaActivity, "Código enviado!", Toast.LENGTH_SHORT).show()
                            layoutEtapaEmail.visibility = View.GONE
                            layoutEtapaCodigo.visibility = View.VISIBLE
                        } else {
                            val erro = response.errorBody()?.string() ?: "Erro ao solicitar código"
                            Toast.makeText(this@RecuperarSenhaActivity, erro, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@RecuperarSenhaActivity, "Falha na conexão", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                etEmail.error = "Campo obrigatório"
            }
        }

        findViewById<Button>(R.id.btnValidarCodigo).setOnClickListener {
            val codigoDigitado = etCodigo.text.toString().trim()
            if (codigoDigitado.length == 6) {
                layoutEtapaCodigo.visibility = View.GONE
                layoutEtapaNovaSenha.visibility = View.VISIBLE
            } else {
                etCodigo.error = "O código deve ter 6 dígitos"
            }
        }

        findViewById<Button>(R.id.btnFinalizarTroca).setOnClickListener {
            val codigo = etCodigo.text.toString().trim()
            val novaSenha = etSenha.text.toString()
            val confirmar = etConfirmar.text.toString()

            if (novaSenha == confirmar && novaSenha.length >= 6) {
                val resetRequest = ResetPasswordRequest(emailUsuario, codigo, novaSenha)

                RetrofitClient.instance.resetarSenha(resetRequest).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RecuperarSenhaActivity, "Senha alterada!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            val erroCorpo = response.errorBody()?.string() ?: "Erro ao redefinir senha"
                            Toast.makeText(this@RecuperarSenhaActivity, erroCorpo, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@RecuperarSenhaActivity, "Erro de rede", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                if (novaSenha.length < 6) {
                    etSenha.error = "Mínimo 6 caracteres"
                } else {
                    Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}