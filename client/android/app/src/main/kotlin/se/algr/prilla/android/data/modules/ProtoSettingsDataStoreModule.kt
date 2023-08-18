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
import se.algr.prilla.android.ProtoSettings
import javax.inject.Singleton
import se.algr.prilla.android.data.serializers.ProtoSettingsSerializer

private const val DATASTORE_FILE_NAME = "settings.pb"

@InstallIn(SingletonComponent::class)
@Module
object ProtoSettingsDataStoreModule {
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<ProtoSettings> =
        DataStoreFactory.create(
            serializer = ProtoSettingsSerializer,
            produceFile = { context.dataStoreFile(DATASTORE_FILE_NAME) }
        )
}
