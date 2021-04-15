package com.almgru.snustrack.android.net

import org.jsoup.Jsoup

object CsrfExtractor {
    fun extractCsrfToken(response: String): String {
        val doc = Jsoup.parse(response)
        val element = doc.select("input[name='_csrf']")
        return element.attr("value")
    }
}