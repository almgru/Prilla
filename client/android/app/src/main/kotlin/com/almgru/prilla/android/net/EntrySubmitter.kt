package com.almgru.prilla.android.net

import com.almgru.prilla.android.model.Entry
import kotlinx.coroutines.Deferred

interface EntrySubmitter {
    fun submit(entry: Entry): Deferred<EntrySubmitResult>
}