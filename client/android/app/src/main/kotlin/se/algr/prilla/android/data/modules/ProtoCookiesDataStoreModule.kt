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
import se.algr.prilla.android.ProtoCookies
import javax.inject.Singleton
import se.algr.prilla.android.data.serializers.ProtoCookiesSerializer

private const val DATASTORE_FILE_NAME = "cookies.pb"

@Module
@InstallIn(SingletonComponent::class)
object ProtoCookiesDataStoreModule {
    @Provides
    @Singleton
    fun provideStateDataStore(@ApplicationContext context: Context): DataStore<ProtoCookies> =
        DataStoreFactory.create(
            serializer = ProtoCookiesSerializer,
            produceFile = { context.dataStoreFile(DATASTORE_FILE_NAME) }
        )
}
