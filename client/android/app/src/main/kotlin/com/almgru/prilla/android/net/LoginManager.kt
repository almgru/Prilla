package com.almgru.prilla.android.net

import com.almgru.prilla.android.net.results.LoginResult

interface LoginManager {
    suspend fun login(username: String, password: String): LoginResult
    suspend fun hasActiveSession(): Boolean
}