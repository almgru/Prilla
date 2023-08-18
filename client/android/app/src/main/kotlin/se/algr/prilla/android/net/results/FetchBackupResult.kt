package se.algr.prilla.android.net.results

sealed class FetchBackupResult {
    data class Success(val json: String) : FetchBackupResult()
    data object SslHandshakeError : FetchBackupResult()
    data object SessionExpiredError : FetchBackupResult()
    data object NetworkError : FetchBackupResult()
}
