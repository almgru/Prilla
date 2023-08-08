package com.almgru.prilla.android.net

import androidx.datastore.core.DataStore
import com.almgru.prilla.android.ProtoSettings
import com.almgru.prilla.android.helpers.CustomClientReadTimeoutRule
import com.almgru.prilla.android.helpers.CustomOkHttpClientReadTimeout
import com.almgru.prilla.android.helpers.MainDispatcherRule
import com.almgru.prilla.android.net.exceptions.UnexpectedHttpStatusException
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
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PrillaHttpClientTest {
    private val cookieJar = mockk<CookieJar>(relaxUnitFun = true)
    private val csrfTokenExtractor = mockk<CsrfTokenExtractor>()
    private val settings = mockk<DataStore<ProtoSettings>>()
    private lateinit var sut: PrillaHttpClient
    private lateinit var mockServer: MockWebServer

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @get:Rule val customClientReadTimeout = CustomClientReadTimeoutRule()

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
            OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .readTimeout(customClientReadTimeout.timeout)
                .build(),
            csrfTokenExtractor,
            settings
        )
    }

    @Test
    fun `login returns Success on 302 Found`() = runTest {
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

    @Test
    fun `login returns InvalidCredentials on 401 Unauthorized`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED))

        every { cookieJar.loadForRequest(any()) } returns emptyList()
        every { csrfTokenExtractor.extractCsrfToken(any()) } returns "csrf_token"

        val result = sut.login("username", "password")

        assertEquals(LoginResult.InvalidCredentials, result)
    }

    @Test(expected = UnexpectedHttpStatusException::class)
    fun `login throws UnexpectedHttpStatusException on unexpected get form status`() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))

        every { cookieJar.loadForRequest(any()) } returns emptyList()

        sut.login("username", "password")
    }

    @Test(expected = UnexpectedHttpStatusException::class)
    fun `login throws UnexpectedHttpStatusException on unexpected post status`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST))

        every { cookieJar.loadForRequest(any()) } returns emptyList()
        every { csrfTokenExtractor.extractCsrfToken(any()) } returns "csrf_token"

        sut.login("username", "password")
    }

    @Test
    @CustomOkHttpClientReadTimeout(timeoutMillis = 2000)
    fun `login returns NetworkError on IOException`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        every { cookieJar.loadForRequest(any()) } returns emptyList()
        every { csrfTokenExtractor.extractCsrfToken(any()) } returns "csrf_token"

        val result = sut.login("username", "password")

        assertTrue(result is LoginResult.NetworkError)
    }

    @Test(expected = Exception::class)
    fun `login propagates other exceptions`() = runTest {
        every { cookieJar.loadForRequest(any()) } throws Exception()

        sut.login("username", "password")
    }
}
