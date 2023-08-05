package com.almgru.prilla.android.net.utilities.csrf

interface CSRFTokenExtractor {
    fun extractCSRFToken(html: String): String
}
