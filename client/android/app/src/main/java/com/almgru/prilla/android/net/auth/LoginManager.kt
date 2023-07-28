package com.almgru.prilla.android.net.auth

import android.content.Context
import com.almgru.prilla.android.PersistenceManager
import com.almgru.prilla.android.net.cookie.CookieStorage
import com.almgru.prilla.android.net.CsrfExtractor
import com.almgru.prilla.android.R
import com.almgru.prilla.android.net.request.LoginRequest
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.net.HttpURLConnection
import java.net.URL

class LoginManager(private var context: Context, private var listener: LoginListener) {
    private var queue: RequestQueue

    init {
        CookieStorage.load(context)
        queue = Volley.newRequestQueue(context, object : HurlStack() {
            override fun createConnection(url: URL?): HttpURLConnection {
                val conn = super.createConnection(url)
                conn.instanceFollowRedirects = false
                return conn
            }
        })
    }

    fun login() {
        queue.add(
            StringRequest(
                Request.Method.GET,
                "${PersistenceManager.getServerUrl(context)}${context.getString(R.string.server_login_success_endpoint)}",
                { listener.onLoggedIn() },
                { onSessionExpired() }
            ))
    }

    fun login(username: String, password: String) {
        // Start by sending a GET request to the login endpoint in order to extract the CSRF token
        queue.add(
            StringRequest(
                Request.Method.GET,
                "${PersistenceManager.getServerUrl(context)}${context.getString(R.string.server_login_endpoint)}",
                { response ->
                    onGetLoginResponse(
                        response,
                        username,
                        password
                    )
                },
                { error -> onRedirectOrError(error) })
        )
    }

    fun hasActiveSession(): Boolean {
        return CookieStorage.hasAuthCookie(context)
    }

    private fun onGetLoginResponse(response: String, username: String, password: String) {
        val csrfToken = CsrfExtractor.extractCsrfToken(response)

        // Send the second request to actually log in
        queue.add(
            LoginRequest(
                "${PersistenceManager.getServerUrl(context)}${context.getString(R.string.server_login_endpoint)}",
                mapOf("username" to username, "password" to password, "_csrf" to csrfToken),
                this::onRedirectOrError
            )
        )
    }

    private fun onRedirectOrError(error: VolleyError?) {
        if (error?.networkResponse == null || error.networkResponse.statusCode != 302) {
            listener.onNetworkError()
        } else if (isBadCredentialsRedirect(error.networkResponse)) {
            listener.onBadCredentials()
        } else if (isLoginSuccessRedirect(error.networkResponse)) {
            CookieStorage.save(context)
            listener.onLoggedIn()
        }
    }

    private fun onSessionExpired() {
        CookieStorage.setAuthCookieExpired(context)
        listener.onSessionExpired()
    }

    private fun isLoginSuccessRedirect(response: NetworkResponse): Boolean {
        return response.headers?.get("Location") ==
                "${PersistenceManager.getServerUrl(context)}${context.getString(R.string.server_login_success_endpoint)}"
    }

    private fun isBadCredentialsRedirect(response: NetworkResponse): Boolean {
        return response.headers?.get("Location") ==
                "${PersistenceManager.getServerUrl(context)}${context.getString(R.string.server_login_failure_endpoint)}"
    }

}