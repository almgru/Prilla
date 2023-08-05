package com.almgru.prilla.android.data.modules

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.almgru.prilla.android.State
import com.almgru.prilla.android.data.serializers.StateSerializer
import java.io.File

object StateDataStoreModule {
    fun provideStateDataStore(): DataStore<State> = DataStoreFactory.create(
        serializer = StateSerializer(),
        produceFile = { File("state.pb") }
    )
}
