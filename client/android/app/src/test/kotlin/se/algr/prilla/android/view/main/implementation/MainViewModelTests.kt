package se.algr.prilla.android.view.main.implementation

import androidx.datastore.core.DataStore
import se.algr.prilla.android.helpers.MainDispatcherRule
import com.google.protobuf.Int32Value
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import se.algr.prilla.android.ProtoEntryState
import se.algr.prilla.android.ProtoSettings
import se.algr.prilla.android.data.Mapper.toProtoTimestamp
import se.algr.prilla.android.data.backup.BackupManager
import se.algr.prilla.android.net.EntrySubmitter
import se.algr.prilla.android.net.results.SubmitResult
import se.algr.prilla.android.utilities.datetimeprovider.DateTimeProvider
import se.algr.prilla.android.view.main.events.MainViewEvent
import se.algr.prilla.android.view.main.state.MainViewState
import java.io.IOException
import java.time.LocalDateTime
import kotlin.time.Duration

class MainViewModelTests {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val submitter = mockk<EntrySubmitter>()
    private val store = FakeEntryStateDataStore()
    private val settings = FakeSettingsDataStore()
    private val dateTimeProvider = FakeDateTimeProvider()
    private val backupManager = mockk<BackupManager>()
    private lateinit var sut: MainViewModel

    @Before
    fun setup() {
        sut = MainViewModel(submitter, store, settings, backupManager, dateTimeProvider)
    }

    @Test
    fun `initializes state from data store`() = runTest(timeout = Duration.parse("1s")) {
        val expectedDateTime = LocalDateTime.parse("2023-08-10T07:00")
        val expectedAmount = 1
        val expectedRecentStarted = LocalDateTime.parse("2023-08-10T05:00")
        val expectedRecentStopped = LocalDateTime.parse("2023-08-10T07:00")
        val expectedRecentAmount = 2

        fun MainViewState.isExpected() = startedDateTime == expectedDateTime &&
                amount == expectedAmount &&
                latestEntry?.started == expectedRecentStarted &&
                latestEntry?.stopped == expectedRecentStopped &&
                latestEntry?.amount == expectedRecentAmount

        store.updateData {
            store.state.value.toBuilder()
                .setCurrentStartedEntry(
                    ProtoEntryState.ProtoStartedEntry.getDefaultInstance().toBuilder()
                        .setStartedAt(expectedDateTime.toProtoTimestamp())
                        .setAmount(Int32Value.of(expectedAmount))
                        .build()
                )
                .setMostRecentlyStoredEntry(
                    ProtoEntryState.ProtoCompleteEntry.getDefaultInstance().toBuilder()
                        .setStartedAt(expectedRecentStarted.toProtoTimestamp())
                        .setStoppedAt(expectedRecentStopped.toProtoTimestamp())
                        .setAmount(Int32Value.of(expectedRecentAmount))
                        .build()
                )
                .build()
        }

        val sut = MainViewModel(submitter, store, settings, backupManager, dateTimeProvider)

        launch {
            sut.state.collect {
                if (it.isExpected()) {
                    cancel()
                }
            }
        }
    }

    @Test
    fun `creates new entry when no current entry`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        launch {
            store.data.collect {
                if (it.hasCurrentStartedEntry()) {
                    cancel()
                }
            }
        }

        sut.onStartStopPressed()
    }

    @Test
    fun `submits started entry`() = runTest {
        val expectedStart = LocalDateTime.parse("2023-08-10T07:00")
        val expectedAmount = 2

        dateTimeProvider.dateTime = expectedStart

        coEvery { submitter.submit(any()) } returns SubmitResult.Success

        sut.onStartStopPressed()
        sut.updateAmount(expectedAmount)
        sut.onStartStopPressed()

        coVerify {
            submitter.submit(match { it.started == expectedStart && it.amount == expectedAmount })
        }
    }

    @Test
    fun `submits custom started entry`() = runTest {
        val expectedStart = LocalDateTime.parse("2023-08-10T08:00")
        val expectedAmount = 3

        coEvery { submitter.submit(any()) } returns SubmitResult.Success

        sut.onStartDateTimePicked(expectedStart)
        sut.updateAmount(expectedAmount)
        sut.onStartStopPressed()

        coVerify {
            submitter.submit(match { it.started == expectedStart && it.amount == expectedAmount })
        }
    }

    @Test
    fun `clears started entry`() = runTest(timeout = Duration.parse("1s")) {
        sut.onStartDateTimePicked(LocalDateTime.parse("2023-08-10T10:00"))

        launch {
            sut.state.collect {
                if (it.startedDateTime == null) {
                    cancel()
                }
            }
        }
        launch {
            store.data.collect {
                if (!it.hasCurrentStartedEntry()) {
                    cancel()
                }
            }
        }

        sut.onStartStopLongPressed()
    }

    @Test
    fun `emits PickStartedDateTimeRequest`() = runTest(timeout = Duration.parse("1s")) {
        val collectIsSetup = Channel<Unit>()

        launch {
            sut.events.onSubscription { collectIsSetup.send(Unit) }.collect {
                if (it is MainViewEvent.PickStartedDatetimeRequest) {
                    cancel()
                }
            }
        }

        collectIsSetup.receive()

        sut.onCustomStartedPressed()
    }

    @Test
    fun `emits CancelledPickStartedDateTime`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        val collectIsSetup = Channel<Unit>()
        val isPickRequestReceived = Channel<Unit>()

        launch {
            sut.events.onSubscription { collectIsSetup.send(Unit) }.collect {
                when (it) {
                    MainViewEvent.PickStartedDatetimeRequest -> isPickRequestReceived.send(Unit)
                    MainViewEvent.CancelledPickStartedDatetime -> cancel()
                    else -> error("Invalid branch for this test")
                }
            }
        }

        collectIsSetup.receive()

        sut.onCustomStartedPressed()
        isPickRequestReceived.receive()

        sut.onCancelPickStartDateTime()
    }

    @Test
    fun `emits PickStoppedDateTimeRequest`() = runTest(timeout = Duration.parse("1s")) {
        val collectIsSetup = Channel<Unit>()
        val isStarted = Channel<Unit>()

        launch {
            sut.events.onSubscription { collectIsSetup.send(Unit) }.collect {
                when (it) {
                    MainViewEvent.EntryStarted -> isStarted.send(Unit)
                    MainViewEvent.PickStoppedDatetimeRequest -> cancel()
                    else -> error("Invalid branch for this test")
                }
            }
        }

        collectIsSetup.receive()

        dateTimeProvider.dateTime = LocalDateTime.parse("2023-08-10T04:00")
        sut.onStartStopPressed()
        isStarted.receive()

        sut.onCustomStoppedPressed()
    }

    @Test
    fun `emits CancelledPickStoppedDateTime`() = runTest(timeout = Duration.parse("1s")) {
        val collectIsSetup = Channel<Unit>()
        val isStarted = Channel<Unit>()
        val isPickerRequestReceived = Channel<Unit>()

        launch {
            sut.events.onSubscription { collectIsSetup.send(Unit) }.collect {
                when (it) {
                    MainViewEvent.EntryStarted -> isStarted.send(Unit)
                    MainViewEvent.PickStoppedDatetimeRequest -> isPickerRequestReceived.send(Unit)
                    MainViewEvent.CancelledPickStoppedDatetime -> cancel()
                    else -> error("Invalid branch for this test")
                }
            }
        }

        collectIsSetup.receive()

        dateTimeProvider.dateTime = LocalDateTime.parse("2023-08-10T02:00")
        sut.onStartStopPressed()
        isStarted.receive()

        sut.onCustomStoppedPressed()
        isPickerRequestReceived.receive()

        sut.onCancelPickStopDateTime()
    }

    @Test
    fun `emits NetworkError`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        val collectIsSetup = Channel<Unit>()
        val isStarted = Channel<Unit>()
        val isSubmitted = Channel<Unit>()

        launch {
            sut.events.onSubscription { collectIsSetup.send(Unit) }.collect {
                when (it) {
                    MainViewEvent.EntryStarted -> isStarted.send(Unit)
                    MainViewEvent.EntrySubmitted -> isSubmitted.send(Unit)
                    MainViewEvent.NetworkError -> cancel()
                    else -> error("Invalid branch for this test")
                }
            }
        }

        collectIsSetup.receive()

        dateTimeProvider.dateTime = LocalDateTime.parse("2023-08-10T15:00")
        sut.onStartStopPressed()
        isStarted.receive()

        coEvery { submitter.submit(any()) } returns SubmitResult.NetworkError(IOException())

        dateTimeProvider.dateTime = LocalDateTime.parse("2023-08-10T16:00")
        sut.onStartStopPressed()
        isSubmitted.receive()
    }

    @Test
    fun `emits InvalidCredentialsError`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        val collectIsSetup = Channel<Unit>()
        val isStarted = Channel<Unit>()
        val isSubmitted = Channel<Unit>()

        launch {
            sut.events.onSubscription { collectIsSetup.send(Unit) }.collect {
                when (it) {
                    MainViewEvent.EntryStarted -> isStarted.send(Unit)
                    MainViewEvent.EntrySubmitted -> isSubmitted.send(Unit)
                    MainViewEvent.InvalidCredentialsError -> cancel()
                    else -> error("Invalid branch for this test")
                }
            }
        }

        collectIsSetup.receive()

        dateTimeProvider.dateTime = LocalDateTime.parse("2023-08-10T20:00")
        sut.onStartStopPressed()
        isStarted.receive()

        coEvery { submitter.submit(any()) } returns SubmitResult.SessionExpiredError

        dateTimeProvider.dateTime = LocalDateTime.parse("2023-08-10T21:30")
        sut.onStartStopPressed()
        isSubmitted.receive()
    }
}

private open class FakeEntryStateDataStore : DataStore<ProtoEntryState> {
    val state = MutableStateFlow<ProtoEntryState>(ProtoEntryState.getDefaultInstance())

    override val data: Flow<ProtoEntryState> = state.asStateFlow()

    override suspend fun updateData(
        transform: suspend (t: ProtoEntryState) -> ProtoEntryState
    ): ProtoEntryState {
        state.update { transform(state.value) }
        return state.value
    }
}

private open class FakeSettingsDataStore : DataStore<ProtoSettings> {
    val state = MutableStateFlow<ProtoSettings>(ProtoSettings.getDefaultInstance())

    override val data: Flow<ProtoSettings> = state.asStateFlow()

    override suspend fun updateData(
        transform: suspend (t: ProtoSettings) -> ProtoSettings
    ): ProtoSettings {
        state.update { transform(state.value) }
        return state.value
    }
}

private class FakeDateTimeProvider : DateTimeProvider {
    var dateTime: LocalDateTime = LocalDateTime.parse("2023-08-11T10:28")
    override fun getCurrentDateTime() = dateTime
}