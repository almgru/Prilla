package com.almgru.prilla.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.almgru.prilla.android.events.Event
import com.almgru.prilla.android.net.auth.LoginListener
import com.almgru.prilla.android.net.auth.LoginManager
import kotlinx.coroutines.flow.MutableStateFlow

class LoginViewModel(
    private val loginManager: LoginManager,
    private val persistenceManager: PersistenceManager
) : ViewModel(), LoginListener {
    enum class LoginViewEvents {
        HAS_ACTIVE_SESSION, SUBMITTED, LOGGED_IN, INVALID_URL_ERROR, BAD_CREDENTIALS_ERROR,
        SESSION_EXPIRED_ERROR, NETWORK_ERROR
    }

    private val _event = MutableLiveData<Event<LoginViewEvents>>(null)
    val event: LiveData<Event<LoginViewEvents>> = _event

    val serverUrl = MutableStateFlow(persistenceManager.getServerUrl() ?: "")
    val username = MutableStateFlow("")
    val password = MutableStateFlow("")

    fun onResume() {
        if (loginManager.hasActiveSession()) {
            _event.value = Event(LoginViewEvents.HAS_ACTIVE_SESSION)
        }
    }

    fun loginWithActiveSession() = loginManager.login()

    fun onLoginPressed() {
        if (isValidUrl(serverUrl.value)) {
            persistenceManager.putServerUrl(serverUrl.value)
            _event.value = Event(LoginViewEvents.SUBMITTED)
            loginManager.login(username.value, password.value)
        } else {
            _event.value = Event(LoginViewEvents.INVALID_URL_ERROR)
        }
    }

    override fun onLoggedIn() {
        _event.value = Event(LoginViewEvents.LOGGED_IN)
    }

    override fun onBadCredentials() {
        _event.value = Event(LoginViewEvents.BAD_CREDENTIALS_ERROR)
    }

    override fun onSessionExpired() {
        _event.value = Event(LoginViewEvents.SESSION_EXPIRED_ERROR)
    }

    override fun onNetworkError() {
        _event.value = Event(LoginViewEvents.NETWORK_ERROR)
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