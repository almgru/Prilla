package se.algr.prilla.android.data.backup.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import se.algr.prilla.android.data.backup.BackupFileManager
import se.algr.prilla.android.data.backup.implementation.DocumentFileBackupFileManager

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupFileManagerModule {
    @Binds
    @Singleton
    abstract fun bindsBackupFileManager(impl: DocumentFileBackupFileManager): BackupFileManager
}
