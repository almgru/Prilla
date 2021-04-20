package com.almgru.prilla.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.almgru.prilla.android.net.auth.LoginListener
import com.almgru.prilla.android.net.auth.LoginManager
import java.time.Period

class LoginActivity : AppCompatActivity(), LoginListener {
    private lateinit var loginManager: LoginManager

    private lateinit var serverField: EditText
    private lateinit var passwordField: EditText
    private lateinit var usernameField: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        serverField = findViewById(R.id.serverField)
        passwordField = findViewById(R.id.passwordField)
        usernameField = findViewById(R.id.usernameField)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.loginProgressBar)

        loginManager = LoginManager(this, this)

        if (loginManager.hasActiveSession()) {
            setLoadingState(true)
            loginManager.login()
        } else {
            serverField.setText(PersistenceManager.getServerUrl(this) ?: "")
        }

        if (PersistenceManager.getUpdateInterval(this) == null) {
            PersistenceManager.putUpdateInterval(
                this,
                Period.parse(getString(R.string.default_preference_update_interval))
            )
        }
    }

    fun onLoginPressed(@Suppress("UNUSED_PARAMETER") view: View) {
        if (isValidUrl(serverField.text.toString())) {
            setLoadingState(true)
            PersistenceManager.putServerUrl(this, serverField.text.toString())
            loginManager.login(usernameField.text.toString(), passwordField.text.toString())
        } else {
            serverField.error = getString(R.string.server_url_validation_error_message)
        }
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

    private fun setLoadingState(loading: Boolean) {
        if (loading) {
            loginButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            loginButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

    private fun isValidUrl(url: String): Boolean {
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            return false
        }

        if (url == "https://" || url == "https://") {
            return false
        }

        return true
    }
}