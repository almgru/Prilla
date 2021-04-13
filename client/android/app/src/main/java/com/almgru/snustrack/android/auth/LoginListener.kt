package com.almgru.snustrack.android.auth

interface LoginListener {
    fun onLoggedIn()
    fun onBadCredentials()
    fun onNetworkError()
}