package com.almgru.prilla.android.net

sealed class EntrySubmitResult {
    data object Success : EntrySubmitResult()
    data object SessionExpiredError : EntrySubmitResult()
    data object NetworkError : EntrySubmitResult()
}