package com.almgru.prilla.android.net

import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.results.RecordEntryResult

interface EntrySubmitter {
    suspend fun submit(entry: Entry): RecordEntryResult
}
