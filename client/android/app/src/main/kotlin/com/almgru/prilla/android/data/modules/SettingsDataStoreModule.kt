package com.almgru.prilla.android.data.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.almgru.prilla.android.Settings
import com.almgru.prilla.android.data.serializers.SettingsSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATASTORE_FILE_NAME = "settings.pb"

@InstallIn(SingletonComponent::class)
@Module
object SettingsDataStoreModule {
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Settings> =
        DataStoreFactory.create(
            serializer = SettingsSerializer,
            produceFile = { context.dataStoreFile(DATASTORE_FILE_NAME) }
        )
}
