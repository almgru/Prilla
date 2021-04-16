package com.almgru.prilla.android.net.request

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest

open class FormPostRequest(
    url: String,
    private var bodyParams: Map<String, String>,
    responseCallback: (String) -> Unit,
    errorCallback: (VolleyError) -> Unit
) : StringRequest(
    POST,
    url,
    Response.Listener(responseCallback),
    Response.ErrorListener(errorCallback)
) {
    init {
        retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    }

    override fun getBodyContentType(): String {
        return "application/x-www-form-urlencoded"
    }

    override fun getParams(): MutableMap<String, String> {
        return bodyParams.toMutableMap()
    }
}