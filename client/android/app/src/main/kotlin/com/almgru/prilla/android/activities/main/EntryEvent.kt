package com.almgru.prilla.android.activities.main

/**
 * Events relating to state management, storage and transmissions of entries.
 */
sealed class EntryEvent {
    /**
     * An entry has been stored successfully on the server.
     */
    data object Stored : EntryEvent()

    /**
     * A previously started entry has been cleared.
     */
    data object Cleared : EntryEvent()

    /**
     * A new entry has been started.
     */
    data object Started : EntryEvent()

    /**
     * A request to store an entry on the server has been submitted.
     */
    data object Submitted : EntryEvent()

    /**
     * A request to open the date & time picker to set the starting time of an entry has been submitted.
     */
    data object PickStartedDatetimeRequest : EntryEvent()

    /**
     * A request to open the date & time picker to set the stopped time of an entry has been submitted.
     */
    data object PickStoppedDatetimeRequest : EntryEvent()

    /**
     * A previous request to open the date & time picker to set a starting time for an entry has been cancelled.
     */
    data object CancelledPickStartedDatetime : EntryEvent()

    /**
     * A previous request to open the date & time picker to set a stopped time for an entry has been cancelled.
     */
    data object CancelledPickStoppedDatetime : EntryEvent()

    /**
     * A previously submitted request to store an entry on the server has gotten an invalid credentials response.
     *
     * In normal circumstances, this indicates that the session cookie used to make requests against the server has
     * expired, and the user will need to login again.
     */
    data object InvalidCredentialsError : EntryEvent()

    /**
     * A network error occurred for a previously submitted request to store an entry on the server
     */
    data object NetworkError : EntryEvent()
}