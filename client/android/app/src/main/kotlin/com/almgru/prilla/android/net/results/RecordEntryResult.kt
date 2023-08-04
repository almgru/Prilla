package com.almgru.prilla.android.net.results

import java.io.IOException

sealed class RecordEntryResult {
    data object Success : RecordEntryResult()
    data object SessionExpiredError : RecordEntryResult()
    data class NetworkError(val exception: IOException) : RecordEntryResult()
}
