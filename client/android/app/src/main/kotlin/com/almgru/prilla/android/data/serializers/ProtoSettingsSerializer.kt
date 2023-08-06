package com.almgru.prilla.android.data.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.almgru.prilla.android.ProtoSettings
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object ProtoSettingsSerializer : Serializer<ProtoSettings> {
    override val defaultValue: ProtoSettings = ProtoSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoSettings = try {
        ProtoSettings.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Unable to read Settings.", exception)
    }

    override suspend fun writeTo(t: ProtoSettings, output: OutputStream) = t.writeTo(output)
}
