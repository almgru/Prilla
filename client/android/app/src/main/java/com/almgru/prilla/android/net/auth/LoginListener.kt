package com.almgru.prilla.android.net.auth

interface LoginListener {
    fun onLoggedIn()
    fun onBadCredentials()
    fun onSessionExpired()
    fun onNetworkError()
}