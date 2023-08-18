package se.algr.prilla.android.net.utilities.csrf

interface CsrfTokenExtractor {
    fun extractCsrfToken(html: String): String
}
