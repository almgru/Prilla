package com.almgru.prilla.android.activities.login

import androidx.datastore.core.DataStore
import com.almgru.prilla.android.ProtoSettings
import com.almgru.prilla.android.helpers.MainDispatcherRule
import com.almgru.prilla.android.net.LoginManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onSubscription
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
    private val settings = mockk<DataStore<ProtoSettings>>()
    private lateinit var sut: LoginViewModel

    @Before
    fun setup() {
        every { settings.data } returns flowOf(ProtoSettings.getDefaultInstance())
        sut = LoginViewModel(loginManager, settings)
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
                .collect {
                    if (it is LoginEvent.CheckingForActiveSession) {
                        cancel() // Cancelling the job will cause the test to exit successfully
                    }
                }
        }

        collectIsSetup.receive() // Wait for event subscription to be ready

        sut.onResume()
    }

    @Test
    fun `onResume should emit HasActiveSession`() = runTest {
    }

    @Test
    fun `onResume should emit NoActiveSession`() = runTest {
    }
}
