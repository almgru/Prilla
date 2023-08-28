package se.algr.prilla.android.data.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import se.algr.prilla.android.ProtoCookies
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
