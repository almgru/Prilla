package se.algr.prilla.android.net.utilities.csrf.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import se.algr.prilla.android.net.utilities.csrf.CsrfTokenExtractor
import se.algr.prilla.android.net.utilities.csrf.implementation.JsoupCsrfTokenExtractor

@Module
@InstallIn(SingletonComponent::class)
object CsrfTokenExtractorModule {
    @Provides
    @Singleton
    fun provideCsrfTokenExtractor(): CsrfTokenExtractor = JsoupCsrfTokenExtractor
}
