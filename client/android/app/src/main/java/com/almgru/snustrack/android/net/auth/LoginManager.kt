package com.almgru.snustrack.android.net.auth

import android.content.Context
import com.almgru.snustrack.android.PersistenceManager
import com.almgru.snustrack.android.net.CookieStorage
import com.almgru.snustrack.android.net.CsrfExtractor
import com.almgru.snustrack.android.R
import com.almgru.snustrack.android.net.request.LoginRequest
import com.android.volley.*
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
            StringRequest(Request.Method.GET,
                "${PersistenceManager.getServerUrl(context)}${context.getString(R.string.server_login_success_endpoint)}",
                Response.Listener<String> { listener.onLoggedIn() },
                Response.ErrorListener { onSessionExpired() }
            ))
    }

    fun login(username: String, password: String) {
        // Start by sending a GET request to the login endpoint in order to extract the CSRF token
        queue.add(
            StringRequest(Request.Method.GET,
                "${PersistenceManager.getServerUrl(context)}${context.getString(R.string.server_login_endpoint)}",
                Response.Listener<String> { response ->
                    onGetLoginResponse(
                        response,
                        username,
                        password
                    )
                },
                Response.ErrorListener { error -> onRedirectOrError(error) })
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