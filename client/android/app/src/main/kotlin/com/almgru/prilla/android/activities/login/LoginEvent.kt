package com.almgru.prilla.android.activities.login

sealed class LoginEvent {
    data object HasActiveSession : LoginEvent()
    data object Submitted : LoginEvent()
    data object LoggedIn : LoginEvent()
    data object InvalidCredentialsError : LoginEvent()
    data object SessionExpiredError : LoginEvent()
    data object NetworkError : LoginEvent()
}
