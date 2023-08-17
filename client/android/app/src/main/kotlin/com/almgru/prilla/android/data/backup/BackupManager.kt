package com.almgru.prilla.android.data.backup

interface BackupManager {
    suspend fun backup(): BackupResult
    suspend fun shouldBackup(): Boolean
}
