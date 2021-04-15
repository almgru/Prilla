package com.almgru.snustrack.android.net.request

import com.almgru.snustrack.android.net.request.FormPostRequest
import com.android.volley.VolleyError

class LoginRequest(
    url: String,
    bodyParams: Map<String, String>,
    callback: (VolleyError) -> Unit
) : FormPostRequest(url, bodyParams, {}, callback)