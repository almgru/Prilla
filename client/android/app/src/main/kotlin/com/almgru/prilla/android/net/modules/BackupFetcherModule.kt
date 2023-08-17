package com.almgru.prilla.android.net.modules

import com.almgru.prilla.android.net.BackupFetcher
import com.almgru.prilla.android.net.PrillaHttpClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupFetcherModule {
    @Binds
    @Singleton
    abstract fun bindBackupFetcher(impl: PrillaHttpClient): BackupFetcher
}
