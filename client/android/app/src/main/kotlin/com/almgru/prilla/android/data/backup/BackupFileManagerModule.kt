package com.almgru.prilla.android.data.backup

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupFileManagerModule {
    @Binds
    @Singleton
    abstract fun bindsBackupFileManager(impl: DocumentFileBackupFileManager): BackupFileManager
}
