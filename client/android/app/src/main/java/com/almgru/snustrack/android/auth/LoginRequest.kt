package com.almgru.snustrack.android.auth

import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest

class LoginRequest(
        url : String,
        private var bodyParams : Map<String, String>,
        callback : (VolleyError) -> Unit
) : StringRequest(Method.POST, url, null, Response.ErrorListener(callback)) {
    override fun getBodyContentType(): String {
        return "application/x-www-form-urlencoded"
    }

    override fun getParams(): MutableMap<String, String> {
        return bodyParams.toMutableMap()
    }
}