package com.almgru.prilla.android.net.utilities.csrf

import org.jsoup.Jsoup

object JsoupCsrfTokenExtractor : CsrfTokenExtractor {
    override fun extractCsrfToken(html: String): String {
        val parsed = Jsoup.parse(html)
        val selected = parsed.select("input[name='_csrf']")
        val value = selected.attr("value")

        return value
    }
}
