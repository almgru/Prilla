package com.almgru.prilla.android.net.cookie

import androidx.datastore.core.DataStore
import com.almgru.prilla.android.ProtoCookies
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PrillaCookieJar @Inject constructor(
    private val cookieStore: DataStore<ProtoCookies>
) : CookieJar {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val cookies: MutableMap<String, List<Cookie>> = mutableMapOf()

    init {
        scope.launch { cookieStore.data.collect { cookies -> loadCookies(cookies) } }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies[url.host] = cookies
        scope.launch { saveCookies(url.host, cookies) }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies
        .getOrDefault(url.host, emptyList())
        .filter { cookie -> cookie.isExpired() || cookie.isForUrl(url) }

    private fun loadCookies(cookies: ProtoCookies) = cookies.cookiesForDomainList.forEach {
        this.cookies[it.domain] = it.cookiesList.map { cookie -> cookie.toOkHttpCookie() }
    }

    private suspend fun saveCookies(domain: String, cookies: List<Cookie>) {
        cookieStore.updateData { current ->
            val index = current.cookiesForDomainList.indexOfFirst { it.domain == domain }
            val toAdd = ProtoCookies.ProtoDomainCookies.newBuilder()
                .setDomain(domain)
                .addAllCookies(cookies.map { it.toProtoCookie() })
                .build()

            current.toBuilder().apply {
                if (index == -1) {
                    addCookiesForDomain(toAdd)
                } else {
                    setCookiesForDomain(index, toAdd)
                }
            }.build()
        }
    }

    private fun Cookie.isExpired() = Instant.ofEpochSecond(expiresAt).isAfter(Instant.now())
    private fun Cookie.isForUrl(url: HttpUrl): Boolean {
        return (path.isEmpty() || url.encodedPath.startsWith(path))
    }

    private fun ProtoCookies.ProtoDomainCookies.ProtoCookie.toOkHttpCookie() = Cookie.Builder()
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

    private fun Cookie.toProtoCookie() = ProtoCookies.ProtoDomainCookies.ProtoCookie.newBuilder()
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
