package com.almgru.prilla.android.model

import java.time.LocalDateTime

data class CompleteEntry(
    val started: LocalDateTime,
    val stopped: LocalDateTime,
    val amount: Int
)
