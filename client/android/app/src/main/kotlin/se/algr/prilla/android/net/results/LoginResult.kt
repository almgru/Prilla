package se.algr.prilla.android.net.results

sealed class LoginResult {
    data object Success : LoginResult()
    data object InvalidCredentials : LoginResult()
    data object SessionExpired : LoginResult()
    data object MalformedUrl : LoginResult()
    data object SslHandshakeError : LoginResult()
    data class NetworkError(val exception: Exception) : LoginResult()
}
