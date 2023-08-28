package se.algr.prilla.android.net

import se.algr.prilla.android.net.results.LoginResult

interface LoginManager {
    suspend fun login(username: String, password: String): LoginResult
    suspend fun hasActiveSession(): Boolean
}
