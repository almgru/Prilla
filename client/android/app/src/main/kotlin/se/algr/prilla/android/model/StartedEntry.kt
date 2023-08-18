package se.algr.prilla.android.model

import java.time.LocalDateTime

data class StartedEntry(
    val started: LocalDateTime,
    val amount: Int
)
