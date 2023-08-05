package com.almgru.prilla.android.data.modules

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.almgru.prilla.android.State
import com.almgru.prilla.android.data.serializers.StateSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

private const val DATASTORE_FILE_NAME = "state.pb"

@InstallIn(SingletonComponent::class)
@Module
object StateDataStoreModule {
    @Provides
    @Singleton
    fun provideStateDataStore(): DataStore<State> = DataStoreFactory.create(
        serializer = StateSerializer,
        produceFile = { File(DATASTORE_FILE_NAME) }
    )
}
