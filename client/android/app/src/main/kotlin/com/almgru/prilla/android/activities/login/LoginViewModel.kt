package com.almgru.prilla.android.activities.login

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almgru.prilla.android.Settings
import com.almgru.prilla.android.net.auth.LoginManager
import com.almgru.prilla.android.net.auth.LoginResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val loginManager: LoginManager, private val settings: DataStore<Settings>) : ViewModel() {
    private val _state = MutableStateFlow(LoginViewState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(serverUrl = settings.data.first().serverUrl) }
        }
    }

    fun onResume() {
        if (loginManager.hasActiveSession()) {
            _events.tryEmit(LoginEvent.HasActiveSession)
        }
    }

    fun loginWithActiveSession() {
        viewModelScope.launch { handleLoginResult(loginManager.login().await()) }
    }

    fun onLoginPressed() {
        if (isValidUrl(state.value.serverUrl)) {
            _events.tryEmit(LoginEvent.Submitted)

            viewModelScope.launch {
                settings.updateData { it.toBuilder().setServerUrl(state.value.serverUrl).build() }
                handleLoginResult(loginManager.login(state.value.username, state.value.password).await())
            }
        } else {
            _events.tryEmit(LoginEvent.InvalidUrlError)
        }
    }

    fun onServerUrlFieldTextChanged(text: String) = _state.update { it.copy(serverUrl = text) }
    fun onUsernameFieldTextChanged(text: String) = _state.update { it.copy(username = text) }
    fun onPasswordFieldTextChanged(text: String) = _state.update { it.copy(password = text) }

    private fun handleLoginResult(result: LoginResult) = when (result) {
        LoginResult.Success -> _events.tryEmit(LoginEvent.LoggedIn)
        LoginResult.InvalidCredentials -> _events.tryEmit(LoginEvent.InvalidCredentialsError)
        LoginResult.SessionExpired -> _events.tryEmit(LoginEvent.SessionExpiredError)
        LoginResult.NetworkError -> _events.tryEmit(LoginEvent.NetworkError)
    }

    // TODO: Move to utility class
    private fun isValidUrl(url: String): Boolean {
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            return false
        }

        if (url == "http://" || url == "https://") {
            return false
        }

        return true
    }
}