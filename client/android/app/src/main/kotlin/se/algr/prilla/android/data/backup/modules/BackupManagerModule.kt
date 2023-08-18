package se.algr.prilla.android.data.backup.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import se.algr.prilla.android.data.backup.BackupManager
import se.algr.prilla.android.data.backup.implementation.PrillaBackupManager

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupManagerModule {
    @Binds
    @Singleton
    abstract fun bindBackupManager(impl: PrillaBackupManager): BackupManager
}
