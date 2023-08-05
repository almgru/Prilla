package com.almgru.prilla.android.data.modules

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.almgru.prilla.android.Settings
import com.almgru.prilla.android.data.serializers.SettingsSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

private const val DATASTORE_FILE_NAME = "settings.pb"

@InstallIn(SingletonComponent::class)
@Module
object SettingsDataStoreModule {
    @Provides
    @Singleton
    fun provideSettingsDataStore(): DataStore<Settings> = DataStoreFactory.create(
        serializer = SettingsSerializer,
        produceFile = { File(DATASTORE_FILE_NAME) }
    )
}
