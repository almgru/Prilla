package com.almgru.prilla.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.almgru.prilla.android.LoginViewModel.LoginViewEvents.BAD_CREDENTIALS_ERROR
import com.almgru.prilla.android.LoginViewModel.LoginViewEvents.HAS_ACTIVE_SESSION
import com.almgru.prilla.android.LoginViewModel.LoginViewEvents.INVALID_URL_ERROR
import com.almgru.prilla.android.LoginViewModel.LoginViewEvents.LOGGED_IN
import com.almgru.prilla.android.LoginViewModel.LoginViewEvents.NETWORK_ERROR
import com.almgru.prilla.android.LoginViewModel.LoginViewEvents.SESSION_EXPIRED_ERROR
import com.almgru.prilla.android.LoginViewModel.LoginViewEvents.SUBMITTED
import com.almgru.prilla.android.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        binding.loginButton.setOnClickListener { viewModel.onLoginPressed() }
        binding.serverField.doOnTextChanged { text, _, _, _ ->
            viewModel.serverUrl.value = text.toString()
        }
        binding.usernameField.doOnTextChanged { text, _, _, _ ->
            viewModel.username.value = text.toString()
        }
        binding.passwordField.doOnTextChanged { text, _, _, _ ->
            viewModel.password.value = text.toString()
        }

        viewModel.event.observe(this) { event ->
            event.getContentIfNotHandled()?.let { content ->
                when (content) {
                    HAS_ACTIVE_SESSION -> viewModel.loginWithActiveSession()
                    SUBMITTED -> setLoadingState(true)
                    LOGGED_IN -> gotoMainActivity()
                    INVALID_URL_ERROR -> showError(R.string.server_url_validation_error_message)
                    BAD_CREDENTIALS_ERROR -> showError(R.string.bad_credentials_error_message)
                    SESSION_EXPIRED_ERROR -> showError(R.string.session_expired_error_message)
                    NETWORK_ERROR -> showError(R.string.network_error_message)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.serverUrl.collect { binding.serverField.setText(it) } }
                launch { viewModel.username.collect { binding.usernameField.setText(it) } }
                launch { viewModel.password.collect { binding.passwordField.setText(it) } }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun gotoMainActivity() {
        setLoadingState(false)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private fun showError(resId: Int) {
        setLoadingState(false)
        Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()
    }

    private fun setLoadingState(loading: Boolean) {
        if (loading) {
            binding.loginButton.visibility = View.GONE
            binding.loginProgressBar.visibility = View.VISIBLE
        } else {
            binding.loginButton.visibility = View.VISIBLE
            binding.loginProgressBar.visibility = View.GONE
        }
    }
}