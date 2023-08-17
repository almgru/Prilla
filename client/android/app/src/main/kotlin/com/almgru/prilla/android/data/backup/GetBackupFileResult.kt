package com.almgru.prilla.android.data.backup

import androidx.documentfile.provider.DocumentFile

sealed class GetBackupFileResult {
    data class Success(val file: DocumentFile) : GetBackupFileResult()
    data object UnsupportedPlatform : GetBackupFileResult()
    data object RequiresPermission : GetBackupFileResult()
}
