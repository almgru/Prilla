package com.almgru.prilla.android.net.modules

import com.almgru.prilla.android.BuildConfig
import com.almgru.prilla.android.net.cookie.PrillaCookieJar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object OkHttpClientModule {
    @Provides
    @Singleton
    fun provideHttpClient(jar: PrillaCookieJar) = OkHttpClient()
        .newBuilder()
        .cookieJar(jar)
        .followRedirects(false)
        .apply {
            if (BuildConfig.DEBUG) {
                addNetworkInterceptor(
                    HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
                )
            }
        }
        .build()
}
