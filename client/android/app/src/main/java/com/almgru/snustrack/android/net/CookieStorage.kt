package com.almgru.snustrack.android.net

import android.content.Context
import com.almgru.snustrack.android.R
import com.fasterxml.jackson.databind.ObjectMapper
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
        val authKey = context.getString(R.string.storage_cookies_auth_key)
        val authCookieName = context.getString(R.string.auth_cookie_name)
        val uri = URI(context.getString(R.string.server_url))
        val prefName = context.getString(R.string.storage_cookies)

        if (cookieManager.cookieStore.cookies.size > 0) {
            val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

            with(prefs.edit()) {
                cookieManager.cookieStore.get(uri).forEach { cookie ->
                    if (cookie.name == authCookieName && !cookie.hasExpired()) {
                        putString(authKey, objectMapper.writeValueAsString(cookie))
                    }
                }

                apply()
            }
        }
    }

    fun load(context: Context) {
        if (hasAuthCookie(context)) {
            return
        }

        val prefName = context.getString(R.string.storage_cookies)
        val authKey = context.getString(R.string.storage_cookies_auth_key)
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val cookieStr = prefs.getString(authKey, null) ?: return
        val cookie = objectMapper.readValue(cookieStr, SerializableHttpCookie::class.java)
            .toHttpCookie()

        if (cookie.hasExpired()) {
            setAuthCookieExpired(
                context
            )
        } else {
            cookieManager.cookieStore.add(URI(cookie.domain), cookie)
        }
    }

    fun hasAuthCookie(context: Context): Boolean {
        val authCookieName = context.getString(R.string.auth_cookie_name)
        val uri = URI(context.getString(R.string.server_url))

        return cookieManager.cookieStore.get(uri).any { cookie ->
            cookie.name == authCookieName && !cookie.hasExpired()
        }
    }

    fun setAuthCookieExpired(context: Context) {
        val authCookieName = context.getString(R.string.auth_cookie_name)
        val prefName = context.getString(R.string.storage_cookies)
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

        with(prefs.edit()) {
            remove(authCookieName)
            apply()
        }

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

        fun toHttpCookie() : HttpCookie {
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

            return cookie
        }
    }
}