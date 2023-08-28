package se.algr.prilla.android.data.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import se.algr.prilla.android.ProtoEntryState
import java.io.InputStream
import java.io.OutputStream

object ProtoEntryStateSerializer : Serializer<ProtoEntryState> {
    override val defaultValue: ProtoEntryState = ProtoEntryState.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoEntryState = try {
        ProtoEntryState.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Unable to read Settings.", exception)
    }

    override suspend fun writeTo(t: ProtoEntryState, output: OutputStream) = t.writeTo(output)
}
