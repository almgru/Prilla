package se.algr.prilla.android.data.backup.implementation

import android.net.Uri
import androidx.datastore.core.DataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import se.algr.prilla.android.ProtoBackupState
import se.algr.prilla.android.ProtoSettings
import se.algr.prilla.android.data.Mapper.toLocalDateTime
import se.algr.prilla.android.data.Mapper.toProtoTimestamp
import se.algr.prilla.android.data.backup.BackupFileManager
import se.algr.prilla.android.data.backup.BackupManager
import se.algr.prilla.android.data.backup.results.BackupResult
import se.algr.prilla.android.data.backup.results.GetBackupFileResult
import se.algr.prilla.android.net.BackupFetcher
import se.algr.prilla.android.net.results.FetchBackupResult
import se.algr.prilla.android.utilities.datetimeprovider.DateTimeProvider

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
                is FetchBackupResult.Success -> when (
                    val writeResult = backupFileManager.writeBackupFile(
                        fetchResult.json,
                        getFileResult.file
                    )
                ) {
                    BackupResult.Success -> writeResult.also { writeTimestamp() }
                    else -> writeResult
                }

                else -> fetchResult.toBackupResult()
            }

            else -> getFileResult.toBackupResult()
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

    private suspend fun writeTimestamp() {
        state.updateData {
            it.toBuilder().setLastBackup(
                dateTimeProvider.getCurrentDateTime().toProtoTimestamp()
            ).build()
        }
    }

    private fun GetBackupFileResult.toBackupResult() = when (this) {
        is GetBackupFileResult.Success -> BackupResult.Success
        GetBackupFileResult.RequiresPermission -> BackupResult.RequiresPermissions
        GetBackupFileResult.UnsupportedPlatform -> BackupResult.UnsupportedPlatformError
    }

    private fun FetchBackupResult.toBackupResult() = when (this) {
        is FetchBackupResult.Success -> BackupResult.Success
        FetchBackupResult.SessionExpiredError -> BackupResult.SessionExpiredError
        FetchBackupResult.SslHandshakeError -> BackupResult.SslHandshakeError
        FetchBackupResult.NetworkError -> BackupResult.NetworkError
    }
}
