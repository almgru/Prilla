package com.almgru.prilla.android

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.almgru.prilla.android.events.Event
import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.EntryAddedListener
import com.almgru.prilla.android.net.EntrySubmitter
import com.android.volley.VolleyError
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime

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

    val event = MutableLiveData<Event<MainViewEvents>>(null)
    val lastEntry = MutableLiveData<Entry?>(null)
    val startedDateTime = MutableLiveData<LocalDateTime?>(null)
    val amount = MutableLiveData(1)

    init {
        submitter.registerListener(this)
    }

    fun onResume() {
        backupManager.backup()
    }

    override fun onEntryAdded() {
        lastEntry.value?.let {
            persistenceManager.putLastEntry(it)
        }

        event.value = Event(MainViewEvents.ENTRY_SUBMIT_SUCCESS)

        handleClear()
    }

    override fun onEntrySubmitError(error: VolleyError) {
        event.value = Event(MainViewEvents.ENTRY_SUBMIT_ERROR)
    }

    fun updateAmount(newAmount: Int) {
        amount.value = newAmount
    }

    fun onStartDateTimePicked(start: LocalDateTime) {
        handleStart(start)
    }

    fun onCancelPickStartDateTime() {
        event.value = Event(MainViewEvents.CANCEL_SELECT_CUSTOM_START_DATETIME)
    }

    fun onStopDateTimePicked(stop: LocalDateTime) {
        handleStop(checkNotNull(startedDateTime.value), stop)
    }

    fun onCancelPickStopDateTime() {
        event.value = Event(MainViewEvents.CANCEL_SELECT_CUSTOM_STOP_DATETIME)
    }

    fun onStartStopPressed() {
        startedDateTime.value?.let { handleStop(it) } ?: run { handleStart() }
    }

    fun onStartStopLongPressed() {
        handleClear()
        event.value = Event(MainViewEvents.ENTRY_CLEARED)
    }

    fun onCustomStartedPressed() {
        event.value = Event(MainViewEvents.SELECT_CUSTOM_START_DATETIME)
    }

    fun onCustomStoppedPressed() {
        requireNotNull(startedDateTime.value)

        event.value = Event(MainViewEvents.SELECT_CUSTOM_STOP_DATETIME)
    }

    private fun handleStart(started: LocalDateTime = LocalDateTime.now()) {
        persistenceManager.putStartedDateTime(started)
        startedDateTime.value = started
        event.value = Event(MainViewEvents.ENTRY_STARTED)
    }

    private fun handleStop(started: LocalDateTime, stopped: LocalDateTime = LocalDateTime.now()) {
        lastEntry.value = Entry(
            started.toKotlinLocalDateTime(),
            stopped.toKotlinLocalDateTime(),
            checkNotNull(amount.value)
        )

        submitter.submit(started, stopped, checkNotNull(amount.value))

        event.value = Event(MainViewEvents.ENTRY_SUBMITTED)
    }

    private fun handleClear() {
        startedDateTime.value = null
        persistenceManager.removeStartedDateTime()
    }
}