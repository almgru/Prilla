package se.algr.prilla.android.data.backup

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import se.algr.prilla.android.data.backup.results.BackupResult
import se.algr.prilla.android.data.backup.results.GetBackupFileResult
import se.algr.prilla.android.data.backup.results.WriteBackupFileResult

interface BackupFileManager {
    fun getBackupFile(directoryUri: Uri): GetBackupFileResult
    fun writeBackupFile(json: String, file: DocumentFile): WriteBackupFileResult
}
