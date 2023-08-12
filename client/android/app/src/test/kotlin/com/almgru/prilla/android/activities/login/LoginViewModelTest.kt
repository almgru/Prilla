package com.almgru.prilla.android.activities.login

import androidx.datastore.core.DataStore
import com.almgru.prilla.android.ProtoSettings
import com.almgru.prilla.android.helpers.MainDispatcherRule
import com.almgru.prilla.android.net.LoginManager
import com.almgru.prilla.android.net.results.LoginResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import java.io.IOException
import kotlin.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val loginManager = mockk<LoginManager>()
    private val store = FakeSettingsDataStore()
    private lateinit var sut: LoginViewModel

    @Before
    fun setup() {
        sut = LoginViewModel(loginManager, store)
    }

    @Test
    fun `initializes serverUrl state from settings`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        val expected = "https://test.example.com"

        store.settings.update { it.toBuilder().setServerUrl(expected).build() }

        val sut = LoginViewModel(loginManager, store)

        launch { sut.state.collect { if (it.serverUrl == expected) { cancel() } } }
    }

    @Test
    fun `onResume should immediately emit CheckingForActiveSession`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        // Define the mocked LoginManager's delay for hasActiveSession, simulating the time it takes
        // to determine if an active session is present.
        val hasActiveSessionDelay = Duration.parse("2s").inWholeMilliseconds

        // Used to make sure the event collection subscription is setup to receive events before the
        // system under test is exercised.
        val collectIsSetup = Channel<Unit>()

        coEvery { loginManager.hasActiveSession() } coAnswers {
            // Since runTest skips delays, we need to run using a separate dispatcher.
            withContext(Dispatchers.Default) {
                runBlocking {
                    delay(hasActiveSessionDelay) // delay to simulate network latency
                }
            }

            true
        }

        launch {
            sut.events
                .onSubscription { collectIsSetup.send(Unit) }
                .collect { if (it is LoginEvent.CheckingForActiveSession) { cancel() } }
        }

        collectIsSetup.receive() // Wait for event subscription to be ready

        sut.onResume()
    }

    @Test
    fun `onResume should emit HasActiveSession`() = runTest(timeout = Duration.parse("1s")) {
        val collectIsSetup = Channel<Unit>()

        coEvery { loginManager.hasActiveSession() } returns true

        launch {
            sut.events
                .onSubscription { collectIsSetup.send(Unit) }
                .collect { if (it is LoginEvent.HasActiveSession) { cancel() } }
        }

        collectIsSetup.receive()

        sut.onResume()
    }

    @Test
    fun `onResume should emit NoActiveSession`() = runTest(timeout = Duration.parse("1s")) {
        val collectIsSetup = Channel<Unit>()

        coEvery { loginManager.hasActiveSession() } returns false

        launch {
            sut.events
                .onSubscription { collectIsSetup.send(Unit) }
                .collect { if (it is LoginEvent.NoActiveSession) { cancel() } }
        }

        collectIsSetup.receive()

        sut.onResume()
    }

    @Test
    fun `onLoginPressed should immediately emit Submitted`() = runTest(
        timeout = Duration.parse("2s")
    ) {
        val loginDelay = Duration.parse("4s").inWholeMilliseconds
        val collectIsSetup = Channel<Unit>()

        coEvery { loginManager.login(any(), any()) } coAnswers {
            withContext(Dispatchers.Default) { runBlocking { delay(loginDelay) } }

            LoginResult.Success
        }

        launch {
            sut.events
                .onSubscription { collectIsSetup.send(Unit) }
                .collect {
                    when (it) {
                        is LoginEvent.Submitted -> cancel()
                        else -> error("Should not be reached")
                    }
                }
        }

        collectIsSetup.receive()

        sut.onLoginPressed()
    }

    @Test
    fun `onLoginPressed should update serverUrl setting`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        val originalServerUrl = "https://original.example.com"
        val updatedServerUrl = "https://updated.example.com"

        store.settings.update { it.toBuilder().setServerUrl(originalServerUrl).build() }

        val sut = LoginViewModel(loginManager, store)

        coEvery { loginManager.login(any(), any()) } returns LoginResult.Success
        launch { store.data.collect { if (it.serverUrl == updatedServerUrl) { cancel() } } }

        sut.onServerUrlFieldTextChanged(updatedServerUrl)
        sut.onLoginPressed()
    }

    @Test
    fun `onLoginPressed should attempt to login using provided credentials`() = runTest {
        val username = "username"
        val password = "password"

        coEvery { loginManager.login(any(), any()) } returns LoginResult.Success

        sut.onUsernameFieldTextChanged(username)
        sut.onPasswordFieldTextChanged(password)
        sut.onLoginPressed()

        coVerify { loginManager.login(username, password) }

        confirmVerified(loginManager)
    }

    @Test
    fun `onLoginPressed should emit LoggedIn on login success`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        val collectIsSetup = Channel<Unit>()

        coEvery { loginManager.login(any(), any()) } returns LoginResult.Success

        launch {
            sut.events
                .onSubscription { collectIsSetup.send(Unit) }
                .collect { if (it is LoginEvent.LoggedIn) { cancel() } }
        }

        collectIsSetup.receive()

        sut.onLoginPressed()
    }

    @Test
    fun `onLoginPressed should emit InvalidCredentialsError on login failed`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        val collectIsSetup = Channel<Unit>()

        coEvery { loginManager.login(any(), any()) } returns LoginResult.InvalidCredentials

        launch {
            sut.events
                .onSubscription { collectIsSetup.send(Unit) }
                .collect { if (it is LoginEvent.InvalidCredentialsError) { cancel() } }
        }

        collectIsSetup.receive()

        sut.onLoginPressed()
    }

    @Test
    fun `onLoginPressed should emit NetworkError on login network error`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        val collectIsSetup = Channel<Unit>()

        coEvery {
            loginManager.login(any(), any())
        } returns LoginResult.NetworkError(IOException())

        launch {
            sut.events
                .onSubscription { collectIsSetup.send(Unit) }
                .collect { if (it is LoginEvent.NetworkError) { cancel() } }
        }

        collectIsSetup.receive()

        sut.onLoginPressed()
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
