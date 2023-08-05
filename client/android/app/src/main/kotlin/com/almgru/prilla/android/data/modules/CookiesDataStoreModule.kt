package com.almgru.prilla.android.data.modules

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.almgru.prilla.android.Cookies
import com.almgru.prilla.android.data.serializers.CookiesSerializer
import java.io.File

object CookiesDataStoreModule {
    fun provideStateDataStore(): DataStore<Cookies> = DataStoreFactory.create(
        serializer = CookiesSerializer(),
        produceFile = { File("cookies.pb") }
    )
}
