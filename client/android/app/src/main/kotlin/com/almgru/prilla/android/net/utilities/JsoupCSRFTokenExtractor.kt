package com.almgru.prilla.android.net.utilities

import org.jsoup.Jsoup

object JsoupCSRFTokenExtractor : CSRFTokenExtractor {
    override fun extractCSRFToken(html: String): String =
            Jsoup.parse(html).select("input[name='_csrf']").attr("value")
}