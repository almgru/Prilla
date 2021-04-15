package com.almgru.snustrack.android.net.auth

interface LoginListener {
    fun onLoggedIn()
    fun onBadCredentials()
    fun onSessionExpired()
    fun onNetworkError()
}