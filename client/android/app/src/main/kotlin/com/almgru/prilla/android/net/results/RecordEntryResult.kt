package com.almgru.prilla.android.net.results

sealed class RecordEntryResult {
    data object Success : RecordEntryResult()
    data object SessionExpiredError : RecordEntryResult()
    data object NetworkError : RecordEntryResult()
}