package com.almgru.prilla.android.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.almgru.prilla.android.State
import com.google.protobuf.InvalidProtocolBufferException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object StateDataStore {
    private object StateSerializer : Serializer<State> {
        override val defaultValue: State = State.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): State = try {
            State.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Unable to read Settings.", exception)
        }

        override suspend fun writeTo(t: State, output: OutputStream) = t.writeTo(output)
    }

    fun provideStateDataStore(context: Context): DataStore<State> = DataStoreFactory.create(
        serializer = StateSerializer,
        produceFile = {
            File("state.pb")
        }
    )
}