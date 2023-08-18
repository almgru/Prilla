package se.algr.prilla.android.net.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import se.algr.prilla.android.net.BackupFetcher
import se.algr.prilla.android.net.implementation.PrillaHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupFetcherModule {
    @Binds
    @Singleton
    abstract fun bindBackupFetcher(impl: PrillaHttpClient): BackupFetcher
}
