package se.algr.prilla.android.data

import com.google.protobuf.Timestamp
import se.algr.prilla.android.ProtoEntryState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import se.algr.prilla.android.model.CompleteEntry
import se.algr.prilla.android.model.StartedEntry

object Mapper {
    fun ProtoEntryState.ProtoCompleteEntry.toModelEntry() = CompleteEntry(
        started = startedAt.toLocalDateTime(),
        stopped = stoppedAt.toLocalDateTime(),
        amount = amount.value
    )

    fun ProtoEntryState.ProtoStartedEntry.toModelEntry() = StartedEntry(
        started = startedAt.toLocalDateTime(),
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
