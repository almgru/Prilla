package com.almgru.prilla.android.activities.main

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almgru.prilla.android.ProtoEntryState
import com.almgru.prilla.android.data.Mapper.toModelEntry
import com.almgru.prilla.android.data.Mapper.toProtoTimestamp
import com.almgru.prilla.android.model.CompleteEntry
import com.almgru.prilla.android.net.EntrySubmitter
import com.almgru.prilla.android.net.results.RecordEntryResult
import com.almgru.prilla.android.utilities.DateTimeProvider
import com.google.protobuf.Int32Value
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
@HiltViewModel
class MainViewModel @Inject constructor(
    private val submitter: EntrySubmitter,
    private val dataStore: DataStore<ProtoEntryState>,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {
    private val _state = MutableStateFlow(MainViewState(null, null, 1))
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<EntryEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update {
                dataStore.data.first().toMainViewState(_state.value)
            }
        }
    }

    fun onStartStopPressed() {
        viewModelScope.launch {
            state.value.startedDateTime?.let {
                handleStop(it)
            } ?: run {
                handleStart()
            }
        }
    }

    fun onStartStopLongPressed() {
        viewModelScope.launch {
            handleClear()
            _events.emit(EntryEvent.Cleared)
        }
    }

    fun updateAmount(newAmount: Int) {
        _state.update { it.copy(amount = newAmount) }
    }

    fun onStartDateTimePicked(start: LocalDateTime) {
        viewModelScope.launch { handleStart(start) }
    }

    fun onCancelPickStartDateTime() {
        viewModelScope.launch { _events.emit(EntryEvent.CancelledPickStartedDatetime) }
    }

    fun onStopDateTimePicked(stop: LocalDateTime) {
        viewModelScope.launch { handleStop(stop) }
    }

    fun onCancelPickStopDateTime() {
        viewModelScope.launch { _events.emit(EntryEvent.CancelledPickStoppedDatetime) }
    }

    fun onCustomStartedPressed() {
        viewModelScope.launch { _events.emit(EntryEvent.PickStartedDatetimeRequest) }
    }

    fun onCustomStoppedPressed() {
        requireNotNull(state.value.startedDateTime)
        viewModelScope.launch { _events.emit(EntryEvent.PickStoppedDatetimeRequest) }
    }

    private suspend fun handleStart(
        started: LocalDateTime = dateTimeProvider.getCurrentDateTime()
    ) {
        dataStore.updateData {
            val startedEntry = it.currentStartedEntry.toBuilder()
                .setStartedAt(started.toProtoTimestamp())
                .setAmount(Int32Value.of(state.value.amount))
            it.toBuilder().setCurrentStartedEntry(startedEntry).build()
        }

        _state.update { it.copy(startedDateTime = started) }
        _events.emit(EntryEvent.Started)
    }

    private suspend fun handleStop(stopped: LocalDateTime = dateTimeProvider.getCurrentDateTime()) {
        val latest = CompleteEntry(
            checkNotNull(state.value.startedDateTime),
            stopped,
            state.value.amount
        )
        _events.emit(EntryEvent.Submitted)

        when (submitter.submit(latest)) {
            RecordEntryResult.Success -> onEntryAdded(latest)
            is RecordEntryResult.NetworkError -> _events.emit(EntryEvent.NetworkError)
            RecordEntryResult.SessionExpiredError -> _events.emit(
                EntryEvent.InvalidCredentialsError
            )
        }
    }

    private suspend fun onEntryAdded(entry: CompleteEntry) {
        dataStore.updateData {
            val toStore = it.mostRecentlyStoredEntry.toBuilder()
                .setStartedAt(entry.started.toProtoTimestamp())
                .setStoppedAt(entry.stopped.toProtoTimestamp())
                .setAmount(Int32Value.of(entry.amount))

            it.toBuilder().setMostRecentlyStoredEntry(toStore).build()
        }

        _state.update { it.copy(latestEntry = entry) }
        _events.emit(EntryEvent.Stored)
        handleClear()
    }

    private suspend fun handleClear() {
        dataStore.updateData { it.toBuilder().clearCurrentStartedEntry().build() }
        _state.update { it.copy(startedDateTime = null) }
    }

    private fun ProtoEntryState.toMainViewState(current: MainViewState): MainViewState {
        val latest = if (hasMostRecentlyStoredEntry()) {
            mostRecentlyStoredEntry.toModelEntry()
        } else {
            current.latestEntry
        }
        val (startedDt, amount) = if (hasCurrentStartedEntry()) {
            val started = currentStartedEntry.toModelEntry()

            Pair(started.started, started.amount)
        } else {
            Pair(current.startedDateTime, current.amount)
        }

        return MainViewState(latest, startedDt, amount)
    }
}
