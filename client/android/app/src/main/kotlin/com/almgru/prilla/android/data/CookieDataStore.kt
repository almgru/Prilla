package com.almgru.prilla.android.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.almgru.prilla.android.Cookies
import com.google.protobuf.InvalidProtocolBufferException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object CookieDataStore {
    private object CookieSerializer : Serializer<Cookies> {
        override val defaultValue: Cookies = Cookies.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): Cookies = try {
            Cookies.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Unable to read cookies.", exception)
        }

        override suspend fun writeTo(t: Cookies, output: OutputStream) = t.writeTo(output)
    }

    fun provideStateDataStore(context: Context): DataStore<Cookies> =
        DataStoreFactory.create(serializer = CookieSerializer, produceFile = {
            File("cookies.pb")
        })
}
