package se.algr.prilla.android.data.backup.implementation

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import se.algr.prilla.android.data.backup.BackupFileManager
import se.algr.prilla.android.data.backup.results.BackupResult
import se.algr.prilla.android.data.backup.results.GetBackupFileResult
import se.algr.prilla.android.data.backup.results.WriteBackupFileResult
import se.algr.prilla.android.utilities.datetimeprovider.DateTimeProvider

class DocumentFileBackupFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dateTimeProvider: DateTimeProvider
) : BackupFileManager {

    override fun getBackupFile(directoryUri: Uri): GetBackupFileResult {
        val now = dateTimeProvider.getCurrentDateTime()
        val fileAppendix = DateTimeFormatter.ISO_DATE_TIME.format(now)
        val fileName = "backup-$fileAppendix.json"

        return DocumentFile.fromTreeUri(context, directoryUri)?.let { dir ->
            dir.createFile("application/json", fileName)?.let { file ->
                GetBackupFileResult.Success(file)
            } ?: GetBackupFileResult.RequiresPermission
        } ?: GetBackupFileResult.UnsupportedPlatform
    }

    override fun writeBackupFile(json: String, file: DocumentFile) = try {
        context.contentResolver.openOutputStream(file.uri, "w").use { stream ->
            if (stream == null) {
                return WriteBackupFileResult.IoError
            }

            stream.bufferedWriter().use { buf -> buf.write(json) }

            WriteBackupFileResult.Success
        }
    } catch (_: SecurityException) {
        WriteBackupFileResult.RequiresPermission
    } catch (_: IOException) {
        WriteBackupFileResult.IoError
    }
}
