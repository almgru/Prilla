package com.almgru.prilla.android.net.auth

import kotlinx.coroutines.Deferred

interface LoginManager {
    fun login(): Deferred<LoginResult>
    fun login(username: String, password: String): Deferred<LoginResult>
    fun hasActiveSession(): Boolean
}