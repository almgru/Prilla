package com.almgru.prilla.android.data.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.almgru.prilla.android.Cookies
import com.almgru.prilla.android.data.serializers.CookiesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATASTORE_FILE_NAME = "cookies.pb"

@Module
@InstallIn(SingletonComponent::class)
object CookiesDataStoreModule {
    @Provides
    @Singleton
    fun provideStateDataStore(@ApplicationContext context: Context): DataStore<Cookies> =
        DataStoreFactory.create(
            serializer = CookiesSerializer,
            produceFile = { context.dataStoreFile(DATASTORE_FILE_NAME) }
        )
}
