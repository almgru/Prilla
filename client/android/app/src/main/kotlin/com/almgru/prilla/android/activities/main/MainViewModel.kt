package com.almgru.prilla.android.activities.main

import androidx.lifecycle.ViewModel
import com.almgru.prilla.android.DataBackupManager
import com.almgru.prilla.android.PersistenceManager
import com.almgru.prilla.android.activities.main.events.CancelSelectCustomStartDateTimeEvent
import com.almgru.prilla.android.activities.main.events.CancelSelectCustomStopDateTimeEvent
import com.almgru.prilla.android.activities.main.events.EntryAddedSuccessfullyEvent
import com.almgru.prilla.android.activities.main.events.EntryClearedEvent
import com.almgru.prilla.android.activities.main.events.EntryStartedEvent
import com.almgru.prilla.android.activities.main.events.EntrySubmitErrorEvent
import com.almgru.prilla.android.activities.main.events.EntrySubmittedEvent
import com.almgru.prilla.android.activities.main.events.SelectCustomStartDateTimeEvent
import com.almgru.prilla.android.activities.main.events.SelectCustomStopDateTimeEvent
import com.almgru.prilla.android.events.Event
import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.EntryAddedListener
import com.almgru.prilla.android.net.EntrySubmitter
import com.android.volley.VolleyError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime

class MainViewModel(
    private val submitter: EntrySubmitter,
    private val backupManager: DataBackupManager,
    private val persistenceManager: PersistenceManager
) : ViewModel(), EntryAddedListener {
    private val _state = MutableStateFlow(
        MainViewState(
            lastEntry = persistenceManager.getLastEntry(),
            startedDateTime = persistenceManager.getStartedDateTime(),
            amount = 0
        )
    )
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    init {
        submitter.registerListener(this)
    }

    fun onResume() = backupManager.backup()

    override fun onEntryAdded() {
        state.value.lastEntry?.let { persistenceManager.putLastEntry(it) }
        handleClear()
        _events.tryEmit(EntryAddedSuccessfullyEvent())
    }

    override fun onEntrySubmitError(error: VolleyError) {
        _events.tryEmit(EntrySubmitErrorEvent())
    }

    fun updateAmount(newAmount: Int) {
        _state.update { it.copy(amount = newAmount) }
        persistenceManager.putAmount(newAmount)
    }

    fun onStartDateTimePicked(start: LocalDateTime) = handleStart(start)

    fun onCancelPickStartDateTime() {
        _events.tryEmit(CancelSelectCustomStartDateTimeEvent())
    }

    fun onStopDateTimePicked(stop: LocalDateTime) =
        handleStop(checkNotNull(state.value.startedDateTime), stop)

    fun onCancelPickStopDateTime() {
        _events.tryEmit(CancelSelectCustomStopDateTimeEvent())
    }

    fun onStartStopPressed() =
        state.value.startedDateTime?.let { handleStop(it) } ?: run { handleStart() }

    fun onStartStopLongPressed() {
        handleClear()
        _events.tryEmit(EntryClearedEvent())
    }

    fun onCustomStartedPressed() {
        _events.tryEmit(SelectCustomStartDateTimeEvent())
    }

    fun onCustomStoppedPressed() {
        requireNotNull(state.value.startedDateTime)
        _events.tryEmit(SelectCustomStopDateTimeEvent())
    }

    private fun handleStart(started: LocalDateTime = LocalDateTime.now()) {
        persistenceManager.putStartedDateTime(started)
        _state.update { it.copy(startedDateTime = started) }
        _events.tryEmit(EntryStartedEvent())
    }

    private fun handleStop(started: LocalDateTime, stopped: LocalDateTime = LocalDateTime.now()) {
        submitter.submit(started, stopped, state.value.amount)

        _state.update {
            it.copy(
                lastEntry = Entry(
                    started.toKotlinLocalDateTime(),
                    stopped.toKotlinLocalDateTime(),
                    state.value.amount
                )
            )
        }

        _events.tryEmit(EntrySubmittedEvent())
    }

    private fun handleClear() {
        persistenceManager.removeStartedDateTime()
        _state.update { it.copy(startedDateTime = null) }
    }
}