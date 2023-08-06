package com.almgru.prilla.android.data

import com.almgru.prilla.android.ProtoEntryState
import com.almgru.prilla.android.model.Entry
import com.google.protobuf.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object Mapper {
    fun ProtoEntryState.ProtoEntry.toModelEntry() = Entry(
        started = startedAt.toLocalDateTime(),
        stopped = stoppedAt.toLocalDateTime(),
        amount = amount.value
    )

    fun LocalDateTime.toProtoTimestamp(): Timestamp {
        val instant = atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp.newBuilder().setSeconds(instant.epochSecond).setNanos(instant.nano).build()
    }

    fun Timestamp.toLocalDateTime(): LocalDateTime = Instant
        .ofEpochSecond(seconds, nanos.toLong())
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}
