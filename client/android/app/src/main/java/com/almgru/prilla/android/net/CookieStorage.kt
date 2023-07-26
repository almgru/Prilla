package com.almgru.prilla.android.net

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.almgru.prilla.android.PersistenceManager
import com.almgru.prilla.android.R
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.IllegalStateException
import java.net.CookieHandler
import java.net.CookieManager
import java.net.HttpCookie
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
                PersistenceManager.putAuthCookie(context, objectMapper.writeValueAsString(cookie))
            }
        }
    }

    fun load(context: Context) {
        if (hasAuthCookie(context)) {
            return
        }

        val cookieStr = PersistenceManager.getAuthCookie(context) ?: return
        val cookie =
            objectMapper.readValue(cookieStr, SerializableHttpCookie::class.java).toHttpCookie()

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

    private class SerializableHttpCookie {
        var comment: String? = null
        var commentURL: String? = null
        var discard: Boolean = false
        var domain: String = ""
        var maxAge: Long = -1
        var name: String = ""
        var path: String = ""
        var portlist: String? = null
        var secure: Boolean = false
        var value: String = ""
        var version: Int = -1
        var httpOnly: Boolean = false

        fun toHttpCookie(): HttpCookie {
            val cookie = HttpCookie(name, value)

            cookie.comment = comment
            cookie.commentURL = commentURL
            cookie.discard = discard
            cookie.domain = domain
            cookie.maxAge = maxAge
            cookie.path = path
            cookie.portlist = portlist
            cookie.secure = secure
            cookie.version = version

            @RequiresApi(Build.VERSION_CODES.N)
            cookie.isHttpOnly = httpOnly

            return cookie
        }
    }
}