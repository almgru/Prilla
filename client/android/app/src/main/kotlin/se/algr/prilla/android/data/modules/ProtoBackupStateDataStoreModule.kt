package se.algr.prilla.android.data.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.algr.prilla.android.ProtoBackupState
import javax.inject.Singleton
import se.algr.prilla.android.data.serializers.ProtoBackupStateSerializer

private const val DATASTORE_FILE_NAME = "backup_state.pb"

@InstallIn(SingletonComponent::class)
@Module
object ProtoBackupStateDataStoreModule {
    @Provides
    @Singleton
    fun provideStateDataStore(@ApplicationContext context: Context): DataStore<ProtoBackupState> =
        DataStoreFactory.create(
            serializer = ProtoBackupStateSerializer,
            produceFile = { context.dataStoreFile(DATASTORE_FILE_NAME) }
        )
}
