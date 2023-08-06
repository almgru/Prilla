package com.almgru.prilla.android.data.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.almgru.prilla.android.ProtoEntryState
import com.almgru.prilla.android.data.serializers.ProtoEntryStateSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATASTORE_FILE_NAME = "state.pb"

@InstallIn(SingletonComponent::class)
@Module
object ProtoEntryStateDataStoreModule {
    @Provides
    @Singleton
    fun provideStateDataStore(@ApplicationContext context: Context): DataStore<ProtoEntryState> =
        DataStoreFactory.create(
            serializer = ProtoEntryStateSerializer,
            produceFile = { context.dataStoreFile(DATASTORE_FILE_NAME) }
        )
}
