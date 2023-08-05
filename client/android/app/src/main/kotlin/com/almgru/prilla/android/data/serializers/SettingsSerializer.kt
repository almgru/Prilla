package com.almgru.prilla.android.data.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.almgru.prilla.android.Settings
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer : Serializer<Settings> {
    override val defaultValue: Settings = Settings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Settings = try {
        Settings.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Unable to read Settings.", exception)
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) = t.writeTo(output)
}
