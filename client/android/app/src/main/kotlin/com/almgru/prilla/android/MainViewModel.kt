package com.almgru.prilla.android

import androidx.lifecycle.ViewModel
import com.almgru.prilla.android.events.Event
import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.EntryAddedListener
import com.almgru.prilla.android.net.EntrySubmitter
import com.android.volley.VolleyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime

data class MainViewState(
    val event: Event<MainViewModel.MainViewEvents>?,
    val lastEntry: Entry?,
    val startedDateTime: LocalDateTime?,
    val amount: Int
)

class MainViewModel(
    private val submitter: EntrySubmitter,
    private val backupManager: DataBackupManager,
    private val persistenceManager: PersistenceManager
) : ViewModel(), EntryAddedListener {
    enum class MainViewEvents {
        ENTRY_STARTED, ENTRY_CLEARED, ENTRY_SUBMITTED, ENTRY_SUBMIT_SUCCESS, ENTRY_SUBMIT_ERROR,
        SELECT_CUSTOM_START_DATETIME, SELECT_CUSTOM_STOP_DATETIME,
        CANCEL_SELECT_CUSTOM_START_DATETIME, CANCEL_SELECT_CUSTOM_STOP_DATETIME
    }

    private val _state = MutableStateFlow(
        MainViewState(
            event = null,
            lastEntry = persistenceManager.getLastEntry(),
            startedDateTime = persistenceManager.getStartedDateTime(),
            amount = 0
        )
    )
    val state: StateFlow<MainViewState> = _state

    init {
        submitter.registerListener(this)
    }

    fun onResume() = backupManager.backup()

    override fun onEntryAdded() {
        state.value.lastEntry?.let { persistenceManager.putLastEntry(it) }
        handleClear()
        _state.update { it.copy(event = Event(MainViewEvents.ENTRY_SUBMIT_SUCCESS)) }
    }

    override fun onEntrySubmitError(error: VolleyError) =
        _state.update { it.copy(event = Event(MainViewEvents.ENTRY_SUBMIT_ERROR)) }

    fun updateAmount(newAmount: Int) {
        _state.update { it.copy(amount = newAmount) }
        persistenceManager.putAmount(newAmount)
    }

    fun onStartDateTimePicked(start: LocalDateTime) = handleStart(start)

    fun onCancelPickStartDateTime() =
        _state.update { it.copy(event = Event(MainViewEvents.CANCEL_SELECT_CUSTOM_START_DATETIME)) }

    fun onStopDateTimePicked(stop: LocalDateTime) =
        handleStop(checkNotNull(state.value.startedDateTime), stop)

    fun onCancelPickStopDateTime() =
        _state.update { it.copy(event = Event(MainViewEvents.CANCEL_SELECT_CUSTOM_STOP_DATETIME)) }

    fun onStartStopPressed() =
        state.value.startedDateTime?.let { handleStop(it) } ?: run { handleStart() }

    fun onStartStopLongPressed() {
        handleClear()
        _state.update { it.copy(event = Event(MainViewEvents.ENTRY_CLEARED)) }
    }

    fun onCustomStartedPressed() =
        _state.update { it.copy(event = Event(MainViewEvents.SELECT_CUSTOM_START_DATETIME)) }

    fun onCustomStoppedPressed() {
        requireNotNull(state.value.startedDateTime)
        _state.update { it.copy(event = Event(MainViewEvents.SELECT_CUSTOM_STOP_DATETIME)) }
    }

    private fun handleStart(started: LocalDateTime = LocalDateTime.now()) {
        persistenceManager.putStartedDateTime(started)
        _state.update {
            it.copy(
                event = Event(MainViewEvents.ENTRY_STARTED),
                startedDateTime = started
            )
        }
    }

    private fun handleStop(started: LocalDateTime, stopped: LocalDateTime = LocalDateTime.now()) {
        submitter.submit(started, stopped, state.value.amount)

        _state.update {
            it.copy(
                event = Event(MainViewEvents.ENTRY_SUBMITTED),
                lastEntry = Entry(
                    started.toKotlinLocalDateTime(),
                    stopped.toKotlinLocalDateTime(),
                    state.value.amount
                )
            )
        }
    }

    private fun handleClear() {
        persistenceManager.removeStartedDateTime()
        _state.update { it.copy(startedDateTime = null) }
    }
}