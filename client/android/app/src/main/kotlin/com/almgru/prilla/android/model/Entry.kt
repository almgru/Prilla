package com.almgru.prilla.android.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Entry(
    val started: @Contextual LocalDateTime,
    val stopped: @Contextual LocalDateTime,
    val amount: Int
)