package se.algr.prilla.android.data.backup.results

sealed class WriteBackupFileResult {
    data object Success : WriteBackupFileResult()
    data object RequiresPermission : WriteBackupFileResult()
    data object IoError : WriteBackupFileResult()
}