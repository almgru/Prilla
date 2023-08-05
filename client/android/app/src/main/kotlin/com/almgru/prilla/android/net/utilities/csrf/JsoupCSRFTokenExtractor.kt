package com.almgru.prilla.android.net.utilities.csrf

import org.jsoup.Jsoup

object JsoupCSRFTokenExtractor : CSRFTokenExtractor {
    override fun extractCSRFToken(html: String): String =
        Jsoup.parse(html).select("input[name='_csrf']").attr("value")
}
