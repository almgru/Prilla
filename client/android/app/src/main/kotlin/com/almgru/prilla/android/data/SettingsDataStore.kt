package com.almgru.prilla.android.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.almgru.prilla.android.Settings
import com.google.protobuf.InvalidProtocolBufferException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object SettingsDataStore {
    private object SettingsSerializer : Serializer<Settings> {
        override val defaultValue: Settings = Settings.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): Settings = try {
            Settings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Unable to read Settings.", exception)
        }

        override suspend fun writeTo(t: Settings, output: OutputStream) = t.writeTo(output)
    }

    fun provideSettingsDataStore(context: Context): DataStore<Settings> =
        DataStoreFactory.create(serializer = SettingsSerializer, produceFile = {
            File("settings.pb")
        })
}