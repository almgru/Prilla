package se.algr.prilla.android.net.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import se.algr.prilla.android.net.LoginManager
import se.algr.prilla.android.net.implementation.PrillaHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginManagerModule {
    @Binds
    @Singleton
    abstract fun bindLoginManager(impl: PrillaHttpClient): LoginManager
}
