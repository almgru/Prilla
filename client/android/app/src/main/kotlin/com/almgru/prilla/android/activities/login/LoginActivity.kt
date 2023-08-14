package com.almgru.prilla.android.activities.login

import android.annotation.SuppressLint
import android.content.Intent
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
import com.almgru.prilla.android.activities.main.MainActivity
import com.almgru.prilla.android.databinding.ActivityLoginBinding
import com.almgru.prilla.android.fragment.ErrorDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        viewModel.onResume()
    }

    override fun onStop() {
        super.onStop()
        setIsLoading(false)
    }

    private fun handleEvent(event: LoginEvent) = when (event) {
        is LoginEvent.CheckingForActiveSession -> setIsLoading(true)
        is LoginEvent.HasActiveSession, is LoginEvent.LoggedIn -> gotoMainActivity()
        is LoginEvent.NoActiveSession -> setIsLoading(false)
        is LoginEvent.Submitted -> setIsLoading(true)
        is LoginEvent.InvalidCredentialsError -> showError(
            R.string.invalid_credentials_error_title,
            R.string.invalid_credentials_error_message
        )
        is LoginEvent.SessionExpiredError -> showError(
            R.string.session_expired_error_title,
            R.string.session_expired_error_message
        )
        is LoginEvent.NetworkError -> showError(
            R.string.network_error_title,
            R.string.network_error_message
        )
    }

    private fun handleStateChange(state: LoginViewState) {
        binding.serverField.setText(state.serverUrl)
        binding.usernameField.setText(state.username)
        binding.passwordField.setText(state.password)
    }

    @SuppressLint("IntentWithNullActionLaunch")
    private fun gotoMainActivity() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
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
        field.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) callback(field.text.toString())
        }
    }
}
