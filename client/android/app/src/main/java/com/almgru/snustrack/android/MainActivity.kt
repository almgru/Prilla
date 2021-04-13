package com.almgru.snustrack.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.almgru.snustrack.android.auth.LoginListener
import com.almgru.snustrack.android.auth.LoginManager
import java.net.CookieHandler
import java.net.CookieManager

class MainActivity : AppCompatActivity(), LoginListener {
    private lateinit var loginManager: LoginManager
    private lateinit var cookies : CookieManager

    private lateinit var passwordField : EditText
    private lateinit var usernameField : EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cookies = CookieManager()
        CookieHandler.setDefault(cookies)
        loginManager = LoginManager(this, this)
        passwordField = findViewById(R.id.passwordField)
        usernameField = findViewById(R.id.usernameField)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.loginProgressBar)
    }

    fun onLoginPressed(view : View) {
        setLoadingState(true)
        loginManager.login(usernameField.text.toString(), passwordField.text.toString())
    }

    override fun onLoggedIn() {
        setLoadingState(false)
        Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
    }

    override fun onBadCredentials() {
        setLoadingState(false)
        Toast.makeText(this, "Bad Credentials", Toast.LENGTH_SHORT).show()
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