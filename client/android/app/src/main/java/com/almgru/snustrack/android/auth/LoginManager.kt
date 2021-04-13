package com.almgru.snustrack.android.auth

import android.content.Context
import com.almgru.snustrack.android.R
import com.android.volley.*
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL

class LoginManager(private var context: Context, private var listener: LoginListener) {
    private var queue : RequestQueue;

    init {
        queue = Volley.newRequestQueue(context, object : HurlStack() {
            override fun createConnection(url: URL?): HttpURLConnection {
                val conn = super.createConnection(url)
                conn.instanceFollowRedirects = false
                return conn
            }
        })
    }

    fun login(username : String, password : String) {
        // Start by sending a GET request to the login endpoint in order to extract the CSRF token
        queue.add(StringRequest(Request.Method.GET,
                "${context.getString(R.string.server_url)}${context.getString(R.string.server_login_endpoint)}",
                Response.Listener<String> { response -> onGetLoginResponse(response, username, password) },
                Response.ErrorListener { error -> onRedirectOrError(error) }))
    }

    private fun onGetLoginResponse(response: String, username: String, password: String) {
        val csrfToken = extractCsrfToken(response)

        // Send the second request to actually log in
        queue.add(LoginRequest("${context.getString(R.string.server_url)}${context.getString(R.string.server_login_endpoint)}",
                mapOf("username" to username, "password" to password, "_csrf" to csrfToken),
                this::onRedirectOrError))
    }

    private fun onRedirectOrError(error: VolleyError?) {
        if (error?.networkResponse == null || error.networkResponse.statusCode != 302) {
            listener.onNetworkError()
        }
        else if (isBadCredentialsRedirect(error.networkResponse)) {
            listener.onBadCredentials()
        } else if (isLoginSuccessRedirect(error.networkResponse)) {
            listener.onLoggedIn()
        }
    }

    private fun isLoginSuccessRedirect(response: NetworkResponse) : Boolean {
        return response.headers?.get("Location") ==
                "${context.getString(R.string.server_url)}${context.getString(R.string.server_login_success_endpoint)}"
    }

    private fun isBadCredentialsRedirect(response : NetworkResponse) : Boolean {
        return response.headers?.get("Location") ==
                "${context.getString(R.string.server_url)}${context.getString(R.string.server_login_failure_endpoint)}"
    }

    private fun extractCsrfToken(response : String) : String {
        val doc = Jsoup.parse(response)
        val element = doc.select("input[name='_csrf']")
        return element.attr("value")
    }
}