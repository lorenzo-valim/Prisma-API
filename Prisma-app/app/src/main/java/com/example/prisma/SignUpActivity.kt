package com.example.prisma

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    private var passoAtual = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val layoutConta = findViewById<LinearLayout>(R.id.layoutEtapaConta)
        val layoutPerfil = findViewById<LinearLayout>(R.id.layoutEtapaPerfil)
        val btnProximo = findViewById<Button>(R.id.btnProximo)
        val tvPasso = findViewById<TextView>(R.id.tvPasso)
        val etNovoEmail = findViewById<EditText>(R.id.etNovoEmail)
        val etNovaSenha = findViewById<EditText>(R.id.etNovaSenha)
        val etNovoNome = findViewById<EditText>(R.id.etNovoNome)
        val etNascimento = findViewById<EditText>(R.id.etNascimento)

        etNascimento.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "##/##/####"
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString().replace(Regex("[^\\d]"), "")
                var result = ""
                if (isUpdating) { isUpdating = false; return }
                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#') { if (str.length > i) result += m; continue }
                    try { result += str[i] } catch (e: Exception) { break }
                    i++
                }
                isUpdating = true
                etNascimento.setText(result)
                etNascimento.setSelection(result.length)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnProximo.setOnClickListener {
            if (passoAtual == 1) {
                val email = etNovoEmail.text.toString().trim()
                val senha = etNovaSenha.text.toString().trim()

                etNovoEmail.error = null
                etNovaSenha.error = null

                if (email.isEmpty()) {
                    etNovoEmail.error = "O e-mail é obrigatório"
                    etNovoEmail.requestFocus()
                } else if (senha.isEmpty()) {
                    etNovaSenha.error = "A senha é obrigatória"
                    etNovaSenha.requestFocus()
                } else if (senha.length < 6) {
                    etNovaSenha.error = "A senha deve ter pelo menos 6 caracteres"
                    etNovaSenha.requestFocus()
                } else {
                    layoutConta.visibility = View.GONE
                    layoutPerfil.visibility = View.VISIBLE
                    tvPasso.text = "Passo 2 de 2"
                    btnProximo.text = "Finalizar Cadastro"
                    passoAtual = 2
                }

            } else {
                val nome = etNovoNome.text.toString().trim()
                val data = etNascimento.text.toString().trim()

                etNovoNome.error = null
                etNascimento.error = null

                if (nome.isEmpty()) {
                    etNovoNome.error = "O nome é obrigatório"
                    etNovoNome.requestFocus()
                } else if (data.isEmpty()) {
                    etNascimento.error = "A data de nascimento é obrigatória"
                    etNascimento.requestFocus()
                } else if (data.length < 10) {
                    etNascimento.error = "Formato inválido: DD/MM/AAAA"
                    etNascimento.requestFocus()
                } else {
                    Toast.makeText(this, "Bem-vindo ao Prisma, $nome!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}