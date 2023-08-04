package com.almgru.prilla.android.net.utilities

interface CSRFTokenExtractor {
    fun extractCSRFToken(html: String): String
}
