package com.almgru.prilla.android.net

import com.almgru.prilla.android.model.CompleteEntry
import com.almgru.prilla.android.net.results.RecordEntryResult

interface EntrySubmitter {
    suspend fun submit(entry: CompleteEntry): RecordEntryResult
}
