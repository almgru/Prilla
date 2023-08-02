package com.almgru.prilla.android.activities.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almgru.prilla.android.PersistenceManager
import com.almgru.prilla.android.activities.login.events.BadCredentialsErrorEvent
import com.almgru.prilla.android.activities.login.events.HasActiveSessionEvent
import com.almgru.prilla.android.activities.login.events.InvalidURLErrorEvent
import com.almgru.prilla.android.activities.login.events.LoggedInSuccessfullyEvent
import com.almgru.prilla.android.activities.login.events.NetworkErrorEvent
import com.almgru.prilla.android.activities.login.events.SubmittedEvent
import com.almgru.prilla.android.events.Event
import com.almgru.prilla.android.net.auth.LoginManager
import com.almgru.prilla.android.net.auth.LoginResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginManager: LoginManager, private val persistenceManager: PersistenceManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        LoginViewState(
            serverUrl = persistenceManager.getServerUrl() ?: "", username = "", password = ""
        )
    )
    val state: StateFlow<LoginViewState> = _state

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()

    fun onResume() {
        if (loginManager.hasActiveSession()) {
            _events.tryEmit(HasActiveSessionEvent())
        }
    }

    fun loginWithActiveSession() {
        viewModelScope.launch {
            handleLoginResult(
                loginManager.login().await()
            )
        }
    }

    fun onLoginPressed() {
        if (isValidUrl(state.value.serverUrl)) {
            persistenceManager.putServerUrl(state.value.serverUrl)
            _events.tryEmit(SubmittedEvent())

            viewModelScope.launch {
                handleLoginResult(
                    loginManager.login(state.value.username, state.value.password).await()
                )
            }
        } else {
            _events.tryEmit(InvalidURLErrorEvent())
        }
    }

    fun onServerUrlFieldTextChanged(text: String) = _state.update { it.copy(serverUrl = text) }
    fun onUsernameFieldTextChanged(text: String) = _state.update { it.copy(username = text) }
    fun onPasswordFieldTextChanged(text: String) = _state.update { it.copy(password = text) }

    private fun handleLoginResult(result: LoginResult) {
        when (result) {
            LoginResult.Success -> _events.tryEmit(LoggedInSuccessfullyEvent())
            LoginResult.InvalidCredentials -> _events.tryEmit(BadCredentialsErrorEvent())
            LoginResult.NetworkError -> _events.tryEmit(NetworkErrorEvent())
        }
    }

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