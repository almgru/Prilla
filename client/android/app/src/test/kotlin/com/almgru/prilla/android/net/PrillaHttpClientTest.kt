package com.almgru.prilla.android.net

import androidx.datastore.core.DataStore
import com.almgru.prilla.android.ProtoSettings
import com.almgru.prilla.android.net.results.LoginResult
import com.almgru.prilla.android.net.utilities.csrf.CsrfTokenExtractor
import io.mockk.every
import io.mockk.mockk
import java.net.HttpURLConnection
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PrillaHttpClientTest {
    private val cookieJar = mockk<CookieJar>(relaxUnitFun = true)
    private val csrfTokenExtractor = mockk<CsrfTokenExtractor>()
    private val settings = mockk<DataStore<ProtoSettings>>()
    private lateinit var sut: PrillaHttpClient
    private lateinit var mockServer: MockWebServer

    @Before
    fun setup() = runTest {
        mockServer = MockWebServer()

        every { settings.data } returns flowOf(
            ProtoSettings.newBuilder()
                .setServerUrl(mockServer.url("/").toString())
                .setBackupIntervalInDays(1)
                .build()
        )

        sut = PrillaHttpClient(
            OkHttpClient.Builder().cookieJar(cookieJar).build(),
            csrfTokenExtractor,
            settings
        )
    }

    @Test
    fun `login should return success when 302 FOUND returned`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP))

        every { cookieJar.loadForRequest(any()) } returns emptyList()
        every { csrfTokenExtractor.extractCsrfToken(any()) } returns "csrf_token"

        val result = sut.login("username", "password")

        assertEquals(LoginResult.Success, result)
    }
}
