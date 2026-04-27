package com.boxmonitor.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.boxmonitor.R

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val PASSWORD = "Admin123++"
        private var failedAttempts = 0
        private var lockUntil = 0L
    }

    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)

        btnLogin.setOnClickListener { attemptLogin() }

        etPassword.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                attemptLogin()
                true
            } else false
        }
    }

    private fun attemptLogin() {
        // Vérifier si bloqué
        if (System.currentTimeMillis() < lockUntil) {
            val remaining = (lockUntil - System.currentTimeMillis()) / 1000
            tvError.text = "Trop de tentatives. Réessayez dans ${remaining}s"
            return
        }

        val input = etPassword.text.toString()
        if (input == PASSWORD) {
            failedAttempts = 0
            etPassword.text.clear()
            tvError.text = ""
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            failedAttempts++
            etPassword.text.clear()
            if (failedAttempts >= 5) {
                lockUntil = System.currentTimeMillis() + 30_000L
                tvError.text = "Trop de tentatives. Bloqué 30 secondes."
                failedAttempts = 0
            } else {
                tvError.text = "Mot de passe incorrect (${failedAttempts}/5)"
            }
        }
    }
}
