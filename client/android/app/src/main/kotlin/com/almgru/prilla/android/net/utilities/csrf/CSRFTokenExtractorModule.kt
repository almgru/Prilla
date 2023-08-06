package com.almgru.prilla.android.net.utilities.csrf

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CsrfTokenExtractorModule {
    @Provides
    @Singleton
    fun provideCsrfTokenExtractor(): CsrfTokenExtractor = JsoupCsrfTokenExtractor
}
