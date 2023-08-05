package com.almgru.prilla.android.net.modules

import com.almgru.prilla.android.net.cookie.PrillaCookieJar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object OkHttpClientModule {
    @Provides
    @Singleton
    fun provideHttpClient(jar: PrillaCookieJar) = OkHttpClient().newBuilder().cookieJar(jar).build()
}