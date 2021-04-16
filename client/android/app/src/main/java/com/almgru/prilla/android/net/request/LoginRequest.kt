package com.almgru.prilla.android.net.request

import com.android.volley.VolleyError

class LoginRequest(
    url: String,
    bodyParams: Map<String, String>,
    callback: (VolleyError) -> Unit
) : FormPostRequest(url, bodyParams, {}, callback)