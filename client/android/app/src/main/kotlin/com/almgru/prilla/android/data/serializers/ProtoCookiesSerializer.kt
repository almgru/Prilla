package com.almgru.prilla.android.data.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.almgru.prilla.android.ProtoCookies
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object ProtoCookiesSerializer : Serializer<ProtoCookies> {
    override val defaultValue: ProtoCookies = ProtoCookies.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoCookies = try {
        ProtoCookies.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Unable to read cookies.", exception)
    }

    override suspend fun writeTo(t: ProtoCookies, output: OutputStream) = t.writeTo(output)
}
