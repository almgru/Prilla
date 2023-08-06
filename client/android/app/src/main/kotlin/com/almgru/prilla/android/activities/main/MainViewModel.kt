package com.almgru.prilla.android.activities.main
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almgru.prilla.android.ProtoEntryState
import com.almgru.prilla.android.data.Mapper.toLocalDateTime
import com.almgru.prilla.android.data.Mapper.toModelEntry
import com.almgru.prilla.android.data.Mapper.toProtoTimestamp
import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.EntrySubmitter
import com.almgru.prilla.android.net.results.RecordEntryResult
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
    private val dataStore: DataStore<ProtoEntryState>
) : ViewModel() {
    private val _state = MutableStateFlow(MainViewState(null, null, 1))
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<EntryEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val stored = dataStore.data.first()
            val newState = MainViewState(
                stored.mostRecentlyStoredEntry?.toModelEntry(),
                stored.currentStartedEntry?.startedAt?.toLocalDateTime(),
                stored.currentStartedEntry.amount.let {
                    if (it.value != 0) it.value else state.value.amount
                }
            )

            _state.update { newState }
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

    fun onCustomStartedPressed() {
        viewModelScope.launch { _events.emit(EntryEvent.PickStartedDatetimeRequest) }
    }

    fun onCustomStoppedPressed() {
        requireNotNull(state.value.startedDateTime)
        viewModelScope.launch { _events.emit(EntryEvent.PickStoppedDatetimeRequest) }
    }

    private suspend fun handleStart(started: LocalDateTime = LocalDateTime.now()) {
        dataStore.updateData {
            val startedEntry = it.currentStartedEntry.toBuilder()
                .setStartedAt(started.toProtoTimestamp())
                .setAmount(Int32Value.of(state.value.amount))
            it.toBuilder().setCurrentStartedEntry(startedEntry).build()
        }

        _state.update { it.copy(startedDateTime = started) }
        _events.emit(EntryEvent.Started)
    }

    private suspend fun handleStop(stopped: LocalDateTime = LocalDateTime.now()) {
        val latest = Entry(checkNotNull(state.value.startedDateTime), stopped, state.value.amount)
        _events.emit(EntryEvent.Submitted)

        when (submitter.submit(latest)) {
            RecordEntryResult.Success -> onEntryAdded(latest)
            is RecordEntryResult.NetworkError -> _events.tryEmit(EntryEvent.NetworkError)
            RecordEntryResult.SessionExpiredError -> _events.tryEmit(
                EntryEvent.InvalidCredentialsError
            )
        }
    }

    private suspend fun onEntryAdded(entry: Entry) {
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
}
