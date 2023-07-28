package com.almgru.prilla.android.net.cookie

import android.content.Context
import com.almgru.prilla.android.PersistenceManager
import com.almgru.prilla.android.R
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.IllegalStateException
import java.net.CookieHandler
import java.net.CookieManager
import java.net.URI

object CookieStorage {
    private val cookieManager: CookieManager = CookieManager()
    private val objectMapper: ObjectMapper = ObjectMapper()

    init {
        CookieHandler.setDefault(cookieManager)
    }

    fun save(context: Context) {
        if (cookieManager.cookieStore.cookies.isEmpty()) {
            return
        }

        val uri = PersistenceManager.getServerUrl(context)
            ?: throw IllegalStateException("Cannot save: Server URL not set")

        val authCookieName = context.getString(R.string.auth_cookie_name)

        cookieManager.cookieStore.get(URI(uri)).forEach { cookie ->
            if (cookie.name == authCookieName && !cookie.hasExpired()) {
                PersistenceManager.putAuthCookie(context, cookie)
            }
        }
    }

    fun load(context: Context) {
        if (hasAuthCookie(context)) {
            return
        }

        val cookie = PersistenceManager.getAuthCookie(context) ?: return

        if (cookie.hasExpired()) {
            setAuthCookieExpired(context)
        } else {
            cookieManager.cookieStore.add(URI(cookie.domain), cookie)
        }
    }

    fun hasAuthCookie(context: Context): Boolean {
        val uri = PersistenceManager.getServerUrl(context) ?: return false

        val authCookieName = context.getString(R.string.auth_cookie_name)

        return cookieManager.cookieStore.get(URI(uri)).any { cookie ->
            cookie.name == authCookieName && !cookie.hasExpired()
        }
    }

    fun setAuthCookieExpired(context: Context) {
        PersistenceManager.removeAuthCookie(context)
        cookieManager.cookieStore.removeAll()
    }
}