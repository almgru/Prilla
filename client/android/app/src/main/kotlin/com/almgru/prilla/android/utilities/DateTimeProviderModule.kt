package com.almgru.prilla.android.utilities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DateTimeProviderModule {
    @Provides
    @Singleton
    fun provideDateTimeProvider(): DateTimeProvider = LocalDateTimeProvider
}
