package com.almgru.prilla.android.activities.login

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almgru.prilla.android.ProtoSettings
import com.almgru.prilla.android.net.LoginManager
import com.almgru.prilla.android.net.results.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginManager: LoginManager,
    private val settings: DataStore<ProtoSettings>
) : ViewModel() {
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
        viewModelScope.launch {
            _events.emit(LoginEvent.CheckingForActiveSession)

            if (loginManager.hasActiveSession()) {
                _events.emit(LoginEvent.HasActiveSession)
            } else {
                _events.emit(LoginEvent.NoActiveSession)
            }
        }
    }

    fun onLoginPressed() {
        viewModelScope.launch {
            _events.emit(LoginEvent.Submitted)
            settings.updateData { it.toBuilder().setServerUrl(state.value.serverUrl).build() }
            handleLoginResult(
                loginManager.login(state.value.username, state.value.password)
            )
        }
    }

    fun onServerUrlFieldTextChanged(text: String) = _state.update { it.copy(serverUrl = text) }
    fun onUsernameFieldTextChanged(text: String) = _state.update { it.copy(username = text) }
    fun onPasswordFieldTextChanged(text: String) = _state.update { it.copy(password = text) }

    private suspend fun handleLoginResult(result: LoginResult) = when (result) {
        LoginResult.Success -> _events.emit(LoginEvent.LoggedIn)
        LoginResult.SessionExpired,
        LoginResult.InvalidCredentials -> _events.emit(LoginEvent.InvalidCredentialsError)
        LoginResult.MalformedUrl -> _events.emit(LoginEvent.MalformedUrlError)
        LoginResult.SslHandshakeError -> _events.emit(LoginEvent.SslHandshakeError)
        is LoginResult.NetworkError -> _events.emit(LoginEvent.NetworkError)
    }
}
