package com.almgru.prilla.android.activities.main

import androidx.datastore.core.DataStore
import com.almgru.prilla.android.ProtoEntryState
import com.almgru.prilla.android.data.Mapper.toProtoTimestamp
import com.almgru.prilla.android.helpers.MainDispatcherRule
import com.almgru.prilla.android.net.EntrySubmitter
import com.almgru.prilla.android.net.results.RecordEntryResult
import com.google.protobuf.Int32Value
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTests {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val submitter = mockk<EntrySubmitter>()
    private lateinit var sut: MainViewModel

    @Before
    fun setup() {
        sut = MainViewModel(submitter, FakeEntryStateDataStore())
    }

    @Test
    fun `initializes state from data store`() = runTest(timeout = Duration.parse("1s")) {
        val expectedDateTime = LocalDateTime.parse("2023-08-10T07:00")
        val expectedAmount = 2

        val store = object : FakeEntryStateDataStore() {
            override val data: Flow<ProtoEntryState> = flowOf(
                ProtoEntryState.getDefaultInstance()
                    .toBuilder()
                    .setCurrentStartedEntry(
                        ProtoEntryState.ProtoStartedEntry.getDefaultInstance().toBuilder()
                            .setStartedAt(expectedDateTime.toProtoTimestamp())
                            .setAmount(Int32Value.of(expectedAmount))
                            .build()
                    )
                    .build()
            )
        }

        val sut = MainViewModel(submitter, store)

        launch {
            sut.state.collect {
                if (it.startedDateTime == expectedDateTime && it.amount == expectedAmount) {
                    cancel()
                }
            }
        }
    }

    @Test
    fun `creates new entry when no current entry`() = runTest(
        timeout = Duration.parse("1s")
    ) {
        val entryStarted = Channel<Unit>()

        val store = object : FakeEntryStateDataStore() {
            override suspend fun updateData(
                transform: suspend (t: ProtoEntryState) -> ProtoEntryState
            ): ProtoEntryState {
                val data = transform.invoke(ProtoEntryState.getDefaultInstance())

                if (data.hasCurrentStartedEntry()) {
                    entryStarted.send(Unit)
                }

                return super.updateData(transform)
            }
        }

        val sut = MainViewModel(submitter, store)

        sut.onStartStopPressed()

        entryStarted.receive()
    }

    @Test
    fun `submits current entry`() = runTest {
        val expectedStart = LocalDateTime.parse("2023-08-10T07:00")
        val expectedAmount = 4

        coEvery { submitter.submit(any()) } returns RecordEntryResult.Success

        sut.onStartDateTimePicked(expectedStart)
        sut.updateAmount(expectedAmount)
        sut.onStartStopPressed()

        coVerify {
            submitter.submit(match { it.started == expectedStart && it.amount == expectedAmount })
        }

        confirmVerified(submitter)
    }
}

private open class FakeEntryStateDataStore : DataStore<ProtoEntryState> {
    override val data: Flow<ProtoEntryState> = flowOf(ProtoEntryState.getDefaultInstance())

    override suspend fun updateData(
        transform: suspend (t: ProtoEntryState) -> ProtoEntryState
    ): ProtoEntryState {
        return ProtoEntryState.getDefaultInstance()
    }
}
