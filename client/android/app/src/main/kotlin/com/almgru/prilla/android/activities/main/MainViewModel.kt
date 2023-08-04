package com.almgru.prilla.android.activities.main
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almgru.prilla.android.State
import com.almgru.prilla.android.data.Mapper.toEntry
import com.almgru.prilla.android.data.Mapper.toLocalDateTime
import com.almgru.prilla.android.data.Mapper.toTimestamp
import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.EntrySubmitter
import com.almgru.prilla.android.net.results.RecordEntryResult
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class MainViewModel(
    private val submitter: EntrySubmitter,
    private val dataStore: DataStore<State>
) : ViewModel() {
    private val _state = MutableStateFlow(MainViewState(null, null, 0))
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<EntryEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val stored = dataStore.data.first()

            _state.update {
                MainViewState(
                    stored.last.toEntry(),
                    stored.started.startedAt.toLocalDateTime(),
                    stored.started.amount
                )
            }
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
            val startedEntry = it.started.toBuilder()
                .setStartedAt(started.toTimestamp())
                .setAmount(state.value.amount)
            it.toBuilder().setStarted(startedEntry).build()
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
            val toStore = it.last.toBuilder()
                .setStartedAt(entry.started.toTimestamp())
                .setStoppedAt(entry.stopped.toTimestamp())
                .setAmount(entry.amount)

            it.toBuilder().setLast(toStore).build()
        }
        _state.update { it.copy(latestEntry = entry) }

        _events.emit(EntryEvent.Stored)
        handleClear()
    }

    private suspend fun handleClear() {
        dataStore.updateData { it.toBuilder().clearStarted().build() }
        _state.update { it.copy(startedDateTime = null) }
    }
}
