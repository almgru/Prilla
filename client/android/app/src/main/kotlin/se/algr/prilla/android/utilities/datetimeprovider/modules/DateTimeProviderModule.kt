package se.algr.prilla.android.utilities.datetimeprovider.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import se.algr.prilla.android.utilities.datetimeprovider.DateTimeProvider
import se.algr.prilla.android.utilities.datetimeprovider.implementation.LocalDateTimeProvider

@Module
@InstallIn(SingletonComponent::class)
object DateTimeProviderModule {
    @Provides
    @Singleton
    fun provideDateTimeProvider(): DateTimeProvider = LocalDateTimeProvider
}
