package com.almgru.prilla.android.activities.login

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almgru.prilla.android.Settings
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
    private val settings: DataStore<Settings>
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
            if (loginManager.hasActiveSession()) {
                _events.emit(LoginEvent.HasActiveSession)
            }
        }
    }

    fun onLoginPressed() {
        viewModelScope.launch {
            _events.emit(LoginEvent.Submitted)
            handleLoginResult(
                loginManager.login(state.value.username, state.value.password)
            )
        }
    }

    fun onServerUrlFieldTextChanged(text: String) = _state.update { it.copy(serverUrl = text) }
    fun onUsernameFieldTextChanged(text: String) = _state.update { it.copy(username = text) }
    fun onPasswordFieldTextChanged(text: String) = _state.update { it.copy(password = text) }

    private suspend fun handleLoginResult(result: LoginResult) = when (result) {
        LoginResult.Success -> {
            settings.updateData { it.toBuilder().setServerUrl(state.value.serverUrl).build() }
            _events.emit(LoginEvent.LoggedIn)
        }

        LoginResult.InvalidCredentials -> _events.emit(LoginEvent.InvalidCredentialsError)
        LoginResult.SessionExpired -> _events.emit(LoginEvent.SessionExpiredError)
        is LoginResult.NetworkError -> _events.emit(LoginEvent.NetworkError)
    }
}
