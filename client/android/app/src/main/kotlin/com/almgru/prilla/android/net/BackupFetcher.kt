package com.almgru.prilla.android.net

import com.almgru.prilla.android.net.results.FetchBackupResult

interface BackupFetcher {
    suspend fun fetchBackup(): FetchBackupResult
}
