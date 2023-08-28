package se.algr.prilla.android.net.implementation

import androidx.datastore.core.DataStore
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import se.algr.prilla.android.ProtoSettings
import se.algr.prilla.android.helpers.CustomClientReadTimeoutRule
import se.algr.prilla.android.helpers.CustomOkHttpClientReadTimeout
import se.algr.prilla.android.helpers.MainDispatcherRule
import se.algr.prilla.android.net.exceptions.UnexpectedHttpStatusException
import se.algr.prilla.android.net.results.LoginResult
import se.algr.prilla.android.net.utilities.csrf.CsrfTokenExtractor
import java.net.HttpURLConnection

class PrillaHttpClientTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val customClientReadTimeout = CustomClientReadTimeoutRule()

    private val csrfTokenExtractor = mockk<CsrfTokenExtractor>()
    private val store = FakeSettingsDataStore()

    private lateinit var sut: PrillaHttpClient
    private lateinit var mockServer: MockWebServer

    @Before
    fun setup() = runTest {
        mockServer = MockWebServer()

        store.settings.update {
            it.toBuilder()
                .setServerUrl(mockServer.url("/").toString())
                .build()
        }

        sut = PrillaHttpClient(
            OkHttpClient.Builder()
                .cookieJar(CookieJar.NO_COOKIES)
                .readTimeout(customClientReadTimeout.timeout)
                .followRedirects(false)
                .build(),
            csrfTokenExtractor,
            store
        )
    }

    @Test
    fun `login returns Success on 302 Found`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setHeader("Location", "/")
        )

        every { csrfTokenExtractor.extractCsrfToken(any()) } returns "csrf_token"

        val result = sut.login("username", "password")

        assertEquals(LoginResult.Success, result)
    }

    @Test
    fun `login returns InvalidCredentials on error redirect`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setHeader("Location", "/error")
        )

        every { csrfTokenExtractor.extractCsrfToken(any()) } returns "csrf_token"

        val result = sut.login("username", "password")

        assertEquals(LoginResult.InvalidCredentials, result)
    }

    @Test
    fun `login returns InvalidCredentials on 401 Unauthorized`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setHeader("Location", "/")
        )

        every { csrfTokenExtractor.extractCsrfToken(any()) } returns "csrf_token"

        val result = sut.login("username", "password")

        assertEquals(LoginResult.InvalidCredentials, result)
    }

    @Test(expected = UnexpectedHttpStatusException::class)
    fun `login throws UnexpectedHttpStatusException on unexpected get form status`() = runTest {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))
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
                .setHeader("Location", "/")
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        every { csrfTokenExtractor.extractCsrfToken(any()) } returns "csrf_token"

        val result = sut.login("username", "password")

        Assert.assertTrue(result is LoginResult.NetworkError)
    }

    @Test(expected = Exception::class)
    fun `login propagates other exceptions`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )

        every { csrfTokenExtractor.extractCsrfToken(any()) } throws Exception()

        sut.login("username", "password")
    }
}

private open class FakeSettingsDataStore : DataStore<ProtoSettings> {
    val settings = MutableStateFlow(ProtoSettings.getDefaultInstance())

    override val data: Flow<ProtoSettings> = settings.asStateFlow()

    override suspend fun updateData(
        transform: suspend (t: ProtoSettings) -> ProtoSettings
    ): ProtoSettings {
        settings.update { transform(settings.value) }
        return settings.value
    }
}