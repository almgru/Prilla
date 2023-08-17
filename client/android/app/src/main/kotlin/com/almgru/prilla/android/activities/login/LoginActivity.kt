package com.almgru.prilla.android.activities.login

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.almgru.prilla.android.R
import com.almgru.prilla.android.activities.errors.ApiError
import com.almgru.prilla.android.activities.main.MainActivity
import com.almgru.prilla.android.databinding.ActivityLoginBinding
import com.almgru.prilla.android.fragment.ErrorDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: ActivityLoginBinding
    private var error: ApiError? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        error = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("error", ApiError::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("error")
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            viewModel.onServerUrlFieldTextChanged(binding.serverField.text.toString())
            viewModel.onUsernameFieldTextChanged(binding.usernameField.text.toString())
            viewModel.onPasswordFieldTextChanged(binding.passwordField.text.toString())
            viewModel.onLoginPressed()
        }

        setupFieldListener(binding.serverField, viewModel::onServerUrlFieldTextChanged)
        setupFieldListener(binding.usernameField, viewModel::onUsernameFieldTextChanged)
        setupFieldListener(binding.passwordField, viewModel::onPasswordFieldTextChanged)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.events.collect(::handleEvent) }
                launch { viewModel.state.collect(::handleStateChange) }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        error?.let {
            when (it) {
                ApiError.SessionExpiredError -> showError(
                    R.string.session_expired_error_title,
                    R.string.session_expired_error_message
                )

                ApiError.SslHandshakeError -> showError(
                    R.string.handshake_error_title,
                    R.string.handshake_error_message
                )

                ApiError.NetworkError -> showError(
                    R.string.network_error_title,
                    R.string.network_error_message
                )
            }

            error = null
        } ?: viewModel.onResume()
    }

    override fun onStop() {
        super.onStop()
        setIsLoading(false)
    }

    private fun handleEvent(event: LoginEvent) = when (event) {
        LoginEvent.CheckingForActiveSession -> setIsLoading(true)
        LoginEvent.HasActiveSession, LoginEvent.LoggedIn -> gotoMainActivity()
        LoginEvent.NoActiveSession -> setIsLoading(false)
        LoginEvent.Submitted -> setIsLoading(true)
        LoginEvent.InvalidCredentialsError -> showError(
            R.string.invalid_credentials_error_title,
            R.string.invalid_credentials_error_message
        )

        LoginEvent.MalformedUrlError -> showError(
            R.string.malformed_url_title,
            R.string.malformed_url_message
        )

        LoginEvent.SslHandshakeError -> showError(
            R.string.handshake_error_title,
            R.string.handshake_error_message
        )

        LoginEvent.NetworkError -> showError(
            R.string.network_error_title,
            R.string.network_error_message
        )
    }

    private fun handleStateChange(state: LoginViewState) {
        binding.serverField.setText(state.serverUrl)
        binding.usernameField.setText(state.username)
        binding.passwordField.setText(state.password)
    }

    private fun gotoMainActivity() {
        startActivity(
            Intent().apply {
                component = ComponentName(this@LoginActivity, MainActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
            }
        )
        finish()
    }

    private fun showError(titleId: Int, messageId: Int) {
        setIsLoading(false)

        ErrorDialogFragment(titleId, messageId).show(
            supportFragmentManager,
            getString(R.string.error_dialog_tag)
        )
    }

    private fun setIsLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loginButton.visibility = View.GONE
            binding.loginProgressBar.visibility = View.VISIBLE
        } else {
            binding.loginButton.visibility = View.VISIBLE
            binding.loginProgressBar.visibility = View.GONE
        }
    }

    private fun setupFieldListener(field: EditText, callback: (String) -> (Unit)) {
        field.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) callback(field.text.toString())
        }
    }
}
