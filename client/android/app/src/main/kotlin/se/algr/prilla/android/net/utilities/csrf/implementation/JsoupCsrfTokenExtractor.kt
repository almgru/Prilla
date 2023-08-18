package se.algr.prilla.android.net.utilities.csrf.implementation

import org.jsoup.Jsoup
import se.algr.prilla.android.net.utilities.csrf.CsrfTokenExtractor

object JsoupCsrfTokenExtractor : CsrfTokenExtractor {
    override fun extractCsrfToken(html: String): String {
        val parsed = Jsoup.parse(html)
        val selected = parsed.select("input[name='_csrf']")
        val value = selected.attr("value")

        return value
    }
}
