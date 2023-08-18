package se.algr.prilla.android.view.login.events

sealed class LoginEvent {
    data object CheckingForActiveSession : LoginEvent()
    data object HasActiveSession : LoginEvent()
    data object NoActiveSession : LoginEvent()
    data object Submitted : LoginEvent()
    data object LoggedIn : LoginEvent()
    data object InvalidCredentialsError : LoginEvent()
    data object MalformedUrlError : LoginEvent()
    data object SslHandshakeError : LoginEvent()
    data object NetworkError : LoginEvent()
}
