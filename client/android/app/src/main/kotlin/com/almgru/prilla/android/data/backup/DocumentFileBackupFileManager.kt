package com.almgru.prilla.android.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.almgru.prilla.android.utilities.DateTimeProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.format.DateTimeFormatter
import javax.inject.Inject

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
                return BackupResult.IoError
            }

            stream.bufferedWriter().use { buf -> buf.write(json) }

            BackupResult.Success
        }
    } catch (_: SecurityException) {
        BackupResult.RequiresPermissions
    } catch (_: IOException) {
        BackupResult.IoError
    }
}
