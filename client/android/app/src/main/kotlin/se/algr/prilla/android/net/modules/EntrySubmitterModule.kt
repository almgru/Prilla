package se.algr.prilla.android.net.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import se.algr.prilla.android.net.EntrySubmitter
import se.algr.prilla.android.net.implementation.PrillaHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class EntrySubmitterModule {
    @Binds
    @Singleton
    abstract fun bindEntrySubmitter(impl: PrillaHttpClient): EntrySubmitter
}
