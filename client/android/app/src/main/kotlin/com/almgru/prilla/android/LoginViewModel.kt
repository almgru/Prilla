package com.almgru.prilla.android

import androidx.lifecycle.ViewModel
import com.almgru.prilla.android.events.Event
import com.almgru.prilla.android.net.auth.LoginListener
import com.almgru.prilla.android.net.auth.LoginManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class LoginViewState(
    val event: Event<LoginViewModel.LoginViewEvents>?,
    val serverUrl: String,
    val username: String,
    val password: String
)

class LoginViewModel(
    private val loginManager: LoginManager,
    private val persistenceManager: PersistenceManager
) : ViewModel(), LoginListener {
    enum class LoginViewEvents {
        HAS_ACTIVE_SESSION, SUBMITTED, LOGGED_IN, INVALID_URL_ERROR, BAD_CREDENTIALS_ERROR,
        SESSION_EXPIRED_ERROR, NETWORK_ERROR
    }

    private val _state = MutableStateFlow(
        LoginViewState(
            event = null,
            serverUrl = persistenceManager.getServerUrl() ?: "",
            username = "",
            password = ""
        )
    )
    val state: StateFlow<LoginViewState> = _state

    fun onResume() {
        if (loginManager.hasActiveSession()) {
            _state.update { it.copy(event = Event(LoginViewEvents.HAS_ACTIVE_SESSION)) }
        }
    }

    fun loginWithActiveSession() = loginManager.login()

    fun onLoginPressed() {
        if (isValidUrl(state.value.serverUrl)) {
            persistenceManager.putServerUrl(state.value.serverUrl)
            _state.update { it.copy(event = Event(LoginViewEvents.SUBMITTED)) }
            loginManager.login(state.value.username, state.value.password)
        } else {
            _state.update { it.copy(event = Event(LoginViewEvents.INVALID_URL_ERROR)) }
        }
    }

    fun onServerUrlFieldTextChanged(text: String) = _state.update { it.copy(serverUrl = text) }
    fun onUsernameFieldTextChanged(text: String) = _state.update { it.copy(username = text) }
    fun onPasswordFieldTextChanged(text: String) = _state.update { it.copy(password = text) }

    override fun onLoggedIn() = _state.update { it.copy(event = Event(LoginViewEvents.LOGGED_IN)) }
    override fun onBadCredentials() = _state.update {
        it.copy(event = Event(LoginViewEvents.BAD_CREDENTIALS_ERROR))
    }

    override fun onSessionExpired() = _state.update {
        it.copy(event = Event(LoginViewEvents.SESSION_EXPIRED_ERROR))
    }

    override fun onNetworkError() = _state.update {
        it.copy(event = Event(LoginViewEvents.NETWORK_ERROR))
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