package com.almgru.prilla.android.net.results

sealed class LoginResult {
    data object Success : LoginResult()
    data object InvalidCredentials : LoginResult()
    data object SessionExpired : LoginResult()
    data object NetworkError : LoginResult()
}
