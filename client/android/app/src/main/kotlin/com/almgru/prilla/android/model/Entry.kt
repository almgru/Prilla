package com.almgru.prilla.android.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Entry(
    val started: LocalDateTime,
    val stopped: LocalDateTime,
    val amount: Int
)