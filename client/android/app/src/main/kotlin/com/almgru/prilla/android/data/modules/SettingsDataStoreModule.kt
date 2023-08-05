package com.almgru.prilla.android.data.modules

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.almgru.prilla.android.Settings
import com.almgru.prilla.android.data.serializers.SettingsSerializer
import java.io.File

object SettingsDataStoreModule {
    fun provideSettingsDataStore(): DataStore<Settings> = DataStoreFactory.create(
        serializer = SettingsSerializer(),
        produceFile = { File("settings.pb") }
    )
}
