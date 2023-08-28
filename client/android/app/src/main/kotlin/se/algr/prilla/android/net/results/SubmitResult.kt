package se.algr.prilla.android.net.results

sealed class SubmitResult {
    data object Success : SubmitResult()
    data object SessionExpiredError : SubmitResult()
    data object SslHandshakeError : SubmitResult()
    data class NetworkError(val exception: Exception) : SubmitResult()
}
