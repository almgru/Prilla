package com.almgru.prilla.android.net.cookie

import androidx.datastore.core.DataStore
import com.almgru.prilla.android.Cookies
import com.almgru.prilla.android.Cookies.UrlCookie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class PrillaCookieJar(
    private val cookieStore: DataStore<Cookies>, private val scope: CoroutineScope
) : CookieJar {
    private val cookies: MutableMap<HttpUrl, List<Cookie>> = mutableMapOf()

    init {
        scope.launch { cookieStore.data.collect { cookies -> loadCookies(cookies) } }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies[url] = cookies
        scope.launch { saveCookies(url, cookies) }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies.getOrDefault(url, emptyList())
    }

    private fun loadCookies(cookies: Cookies) {
        cookies.cookiesForUrlList.forEach { urlCookies ->
            urlCookies.url.toHttpUrlOrNull()?.let { url ->
                this.cookies[url] = urlCookies.cookiesList.map { cookie -> cookie.toOkHttpCookie() }
            }
        }
    }

    private suspend fun saveCookies(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.updateData { current ->
            val index = current.cookiesForUrlList.indexOfFirst { it.url == url.toString() }
            val toAdd = UrlCookie.newBuilder()
                .setUrl(url.toString())
                .addAllCookies(cookies.map { it.toProtoCookie() })
                .build()

            current.toBuilder().apply {
                if (index == -1) {
                    addCookiesForUrl(toAdd)
                } else {
                    setCookiesForUrl(index, toAdd)
                }
            }.build()
        }
    }

    private fun UrlCookie.Cookie.toOkHttpCookie() = Cookie.Builder()
        .name(name)
        .value(value)
        .domain(domain)
        .path(path)
        .expiresAt(expiresAt)
        .apply {
            if (secure) secure()
            if (httpOnly) httpOnly()
            if (hostOnly) hostOnlyDomain(domain)
        }
        .build()

    private fun Cookie.toProtoCookie() = UrlCookie.Cookie.newBuilder()
        .setName(name)
        .setValue(value)
        .setDomain(domain)
        .setPath(path)
        .setExpiresAt(expiresAt)
        .setSecure(secure)
        .setHttpOnly(httpOnly)
        .setHostOnly(hostOnly)
        .build()
}
