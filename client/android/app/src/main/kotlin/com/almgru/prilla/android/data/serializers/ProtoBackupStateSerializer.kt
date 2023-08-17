package com.almgru.prilla.android.data.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.almgru.prilla.android.ProtoBackupState
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object ProtoBackupStateSerializer : Serializer<ProtoBackupState> {
    override val defaultValue: ProtoBackupState = ProtoBackupState.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoBackupState = try {
        ProtoBackupState.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Unable to read Settings.", exception)
    }

    override suspend fun writeTo(t: ProtoBackupState, output: OutputStream) = t.writeTo(output)
}
