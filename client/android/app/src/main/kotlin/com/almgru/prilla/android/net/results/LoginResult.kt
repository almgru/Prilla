package com.almgru.prilla.android.net.results

import java.io.IOException

sealed class LoginResult {
    data object Success : LoginResult()
    data object InvalidCredentials : LoginResult()
    data object SessionExpired : LoginResult()
    data class NetworkError(val exception: IOException) : LoginResult()
}
