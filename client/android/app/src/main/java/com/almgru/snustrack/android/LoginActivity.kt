package com.almgru.snustrack.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.almgru.snustrack.android.net.auth.LoginListener
import com.almgru.snustrack.android.net.auth.LoginManager

class LoginActivity : AppCompatActivity(), LoginListener {
    private lateinit var loginManager: LoginManager

    private lateinit var passwordField : EditText
    private lateinit var usernameField : EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        passwordField = findViewById(R.id.passwordField)
        usernameField = findViewById(R.id.usernameField)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.loginProgressBar)

        loginManager = LoginManager(this, this)

        if (loginManager.hasActiveSession()) {
            setLoadingState(true)
            loginManager.login()
        }
    }

    fun onLoginPressed(@Suppress("UNUSED_PARAMETER") view : View) {
        setLoadingState(true)
        loginManager.login(usernameField.text.toString(), passwordField.text.toString())
    }

    override fun onLoggedIn() {
        setLoadingState(false)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    override fun onBadCredentials() {
        setLoadingState(false)
        Toast.makeText(this, "Bad Credentials", Toast.LENGTH_SHORT).show()
    }

    override fun onSessionExpired() {
        setLoadingState(false)
        Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
    }

    override fun onNetworkError() {
        setLoadingState(false)
        Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
    }

    private fun setLoadingState(loading : Boolean) {
        if (loading) {
            loginButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            loginButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}