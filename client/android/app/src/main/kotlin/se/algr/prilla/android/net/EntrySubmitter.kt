package se.algr.prilla.android.net

import se.algr.prilla.android.model.CompleteEntry
import se.algr.prilla.android.net.results.SubmitResult

interface EntrySubmitter {
    suspend fun submit(entry: CompleteEntry): SubmitResult
}
