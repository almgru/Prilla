package se.algr.prilla.android.net

import se.algr.prilla.android.net.results.FetchBackupResult

interface BackupFetcher {
    suspend fun fetchBackup(): FetchBackupResult
}
