package se.algr.prilla.android.data.backup

import se.algr.prilla.android.data.backup.results.BackupResult

interface BackupManager {
    suspend fun backup(): BackupResult
    suspend fun shouldBackup(): Boolean
}
