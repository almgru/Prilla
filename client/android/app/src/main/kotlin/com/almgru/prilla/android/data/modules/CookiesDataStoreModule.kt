package com.almgru.prilla.android.data.modules

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.almgru.prilla.android.Cookies
import com.almgru.prilla.android.data.serializers.CookiesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

private const val DATASTORE_FILE_NAME = "cookies.pb"

@Module
@InstallIn(SingletonComponent::class)
object CookiesDataStoreModule {
    @Provides
    @Singleton
    fun provideStateDataStore(): DataStore<Cookies> = DataStoreFactory.create(
        serializer = CookiesSerializer,
        produceFile = { File(DATASTORE_FILE_NAME) }
    )
}
