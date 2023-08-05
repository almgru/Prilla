package com.almgru.prilla.android.net.modules

import com.almgru.prilla.android.net.LoginManager
import com.almgru.prilla.android.net.PrillaHttpClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginManagerModule {
    @Binds
    @Singleton
    abstract fun bindLoginManager(impl: PrillaHttpClient): LoginManager
}
