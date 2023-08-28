package se.algr.prilla.android.net.exceptions

data class UnexpectedHttpStatusException(
    val statusCode: Int,
    val statusMessage: String
) : Exception()
