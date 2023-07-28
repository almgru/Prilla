package com.almgru.prilla.android.net.cookie;

import android.os.Build
import kotlinx.serialization.Serializable
import java.net.HttpCookie

@Serializable
data class SerializableHttpCookie(
    val comment: String?,
    val commentURL: String?,
    val discard: Boolean,
    val domain: String,
    val maxAge: Long,
    val name: String,
    val path: String,
    val portlist: String?,
    val secure: Boolean,
    val value: String,
    val version: Int,
    val httpOnly: Boolean
) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cookie.isHttpOnly = httpOnly
        }

        return cookie
    }
}
