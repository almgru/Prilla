package com.almgru.prilla.android.data.backup

import android.net.Uri
import androidx.documentfile.provider.DocumentFile

interface BackupFileManager {
    fun getBackupFile(directoryUri: Uri): GetBackupFileResult
    fun writeBackupFile(json: String, file: DocumentFile): BackupResult
}
