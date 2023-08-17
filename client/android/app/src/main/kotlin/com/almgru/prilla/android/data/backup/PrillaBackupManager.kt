package com.almgru.prilla.android.data.backup

import android.net.Uri
import androidx.datastore.core.DataStore
import com.almgru.prilla.android.ProtoBackupState
import com.almgru.prilla.android.ProtoSettings
import com.almgru.prilla.android.data.Mapper.toLocalDateTime
import com.almgru.prilla.android.data.Mapper.toProtoTimestamp
import com.almgru.prilla.android.net.BackupFetcher
import com.almgru.prilla.android.net.results.FetchBackupResult
import com.almgru.prilla.android.utilities.DateTimeProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.first

private const val DEFAULT_BACKUP_INTERVAL_IN_DAYS = 1

class PrillaBackupManager @Inject constructor(
    private val fetcher: BackupFetcher,
    private val dateTimeProvider: DateTimeProvider,
    private val settings: DataStore<ProtoSettings>,
    private val state: DataStore<ProtoBackupState>,
    private val backupFileManager: BackupFileManager
) : BackupManager {
    override suspend fun backup(): BackupResult {
        val directoryUri = if (settings.data.first().hasBackupDirectoryUri()) {
            Uri.parse(settings.data.first().backupDirectoryUri)
        } else {
            return BackupResult.RequiresPermissions
        }

        return when (val getFileResult = backupFileManager.getBackupFile(directoryUri)) {
            is GetBackupFileResult.Success -> when (val fetchResult = fetcher.fetchBackup()) {
                is FetchBackupResult.Success -> {
                    val result = backupFileManager.writeBackupFile(
                        fetchResult.json,
                        getFileResult.file
                    )

                    val now = dateTimeProvider.getCurrentDateTime()

                    state.updateData {
                        it.toBuilder().setLastBackup(now.toProtoTimestamp()).build()
                    }

                    return result
                }

                FetchBackupResult.SslHandshakeError -> BackupResult.SslHandshakeError
                FetchBackupResult.SessionExpiredError -> BackupResult.SessionExpiredError
                FetchBackupResult.NetworkError -> BackupResult.NetworkError
            }

            GetBackupFileResult.RequiresPermission -> BackupResult.RequiresPermissions
            GetBackupFileResult.UnsupportedPlatform -> BackupResult.UnsupportedPlatformError
            GetBackupFileResult.RequiresPermission -> TODO()
        }
    }

    override suspend fun shouldBackup(): Boolean {
        val lastBackup = state.data.first().lastBackup.toLocalDateTime()
        val now = dateTimeProvider.getCurrentDateTime()
        val interval = if (settings.data.first().hasBackupIntervalInDays()) {
            settings.data.first().backupIntervalInDays
        } else {
            settings.updateData {
                it.toBuilder().setBackupIntervalInDays(DEFAULT_BACKUP_INTERVAL_IN_DAYS).build()
            }

            DEFAULT_BACKUP_INTERVAL_IN_DAYS
        }

        return now.isAfter(lastBackup.plusDays(interval.toLong()))
    }
}
