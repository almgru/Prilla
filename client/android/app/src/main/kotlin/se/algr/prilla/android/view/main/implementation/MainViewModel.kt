package se.algr.prilla.android.view.main.implementation

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.Int32Value
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.algr.prilla.android.ProtoEntryState
import se.algr.prilla.android.ProtoSettings
import se.algr.prilla.android.data.Mapper.toModelEntry
import se.algr.prilla.android.data.Mapper.toProtoTimestamp
import se.algr.prilla.android.data.backup.BackupManager
import se.algr.prilla.android.data.backup.results.BackupResult
import se.algr.prilla.android.model.CompleteEntry
import se.algr.prilla.android.net.EntrySubmitter
import se.algr.prilla.android.net.results.SubmitResult
import se.algr.prilla.android.utilities.datetimeprovider.DateTimeProvider
import se.algr.prilla.android.view.main.events.MainViewEvent
import se.algr.prilla.android.view.main.state.MainViewState

@Suppress("TooManyFunctions")
@HiltViewModel
class MainViewModel @Inject constructor(
    private val submitter: EntrySubmitter,
    private val dataStore: DataStore<ProtoEntryState>,
    private val settingsStore: DataStore<ProtoSettings>,
    private val backupManager: BackupManager,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {
    private val _state = MutableStateFlow(MainViewState(null, null, 1))
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainViewEvent>()
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
            _events.emit(MainViewEvent.EntryCleared)
        }
    }

    fun updateAmount(newAmount: Int) {
        _state.update { it.copy(amount = newAmount) }
    }

    fun onStartDateTimePicked(start: LocalDateTime) {
        viewModelScope.launch { handleStart(start) }
    }

    fun onCancelPickStartDateTime() {
        viewModelScope.launch { _events.emit(MainViewEvent.CancelledPickStartedDatetime) }
    }

    fun onStopDateTimePicked(stop: LocalDateTime) {
        viewModelScope.launch { handleStop(stop) }
    }

    fun onCancelPickStopDateTime() {
        viewModelScope.launch { _events.emit(MainViewEvent.CancelledPickStoppedDatetime) }
    }

    fun onCustomStartedPressed() {
        viewModelScope.launch { _events.emit(MainViewEvent.PickStartedDatetimeRequest) }
    }

    fun onCustomStoppedPressed() {
        requireNotNull(state.value.startedDateTime)
        viewModelScope.launch { _events.emit(MainViewEvent.PickStoppedDatetimeRequest) }
    }

    fun onStoragePermissionGranted(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsStore.updateData {
                it.toBuilder().setBackupDirectoryUri(uri.toString()).build()
            }

            backupManager.backup()
        }
    }

    fun onResume() {
        viewModelScope.launch(Dispatchers.IO) { backup() }
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
        _events.emit(MainViewEvent.EntryStarted)
    }

    private suspend fun handleStop(stopped: LocalDateTime = dateTimeProvider.getCurrentDateTime()) {
        val latest = CompleteEntry(
            checkNotNull(state.value.startedDateTime),
            stopped,
            state.value.amount
        )
        _events.emit(MainViewEvent.EntrySubmitted)

        when (submitter.submit(latest)) {
            SubmitResult.Success -> onEntryAdded(latest)
            SubmitResult.SslHandshakeError -> _events.emit(MainViewEvent.SslHandshakeError)
            SubmitResult.SessionExpiredError -> _events.emit(MainViewEvent.InvalidCredentialsError)
            is SubmitResult.NetworkError -> _events.emit(MainViewEvent.NetworkError)
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
        _events.emit(MainViewEvent.EntryStored)
        handleClear()
    }

    private suspend fun handleClear() {
        dataStore.updateData { it.toBuilder().clearCurrentStartedEntry().build() }
        _state.update { it.copy(startedDateTime = null) }
    }

    private suspend fun backup() {
        if (backupManager.shouldBackup()) {
            handleBackupResult(backupManager.backup())
        }
    }

    private suspend fun handleBackupResult(result: BackupResult) = when (result) {
        BackupResult.Success -> _events.emit(MainViewEvent.BackupSuccessful)
        BackupResult.RequiresPermissions -> _events.emit(MainViewEvent.BackupRequiresPermission)
        BackupResult.SessionExpiredError -> _events.emit(MainViewEvent.InvalidCredentialsError)
        BackupResult.SslHandshakeError -> _events.emit(MainViewEvent.SslHandshakeError)
        BackupResult.UnsupportedPlatformError -> _events.emit(MainViewEvent.BackupUnsupported)
        BackupResult.NetworkError -> _events.emit(MainViewEvent.NetworkError)
        BackupResult.IoError -> _events.emit(MainViewEvent.BackupIoError)
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
