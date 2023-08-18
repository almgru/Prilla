package se.algr.prilla.android.data.backup.results

sealed class BackupResult {
    data object Success : BackupResult()
    data object RequiresPermissions : BackupResult()
    data object UnsupportedPlatformError : BackupResult()
    data object IoError : BackupResult()
    data object SessionExpiredError : BackupResult()
    data object SslHandshakeError : BackupResult()
    data object NetworkError : BackupResult()
}
