package com.almgru.prilla.android.activities.main

/**
 * Events relating to state management, storage and transmissions of entries.
 */
sealed class MainViewEvent {
    /**
     * An entry has been stored successfully on the server.
     */
    data object EntryStored : MainViewEvent()

    /**
     * A previously started entry has been cleared.
     */
    data object EntryCleared : MainViewEvent()

    /**
     * A new entry has been started.
     */
    data object EntryStarted : MainViewEvent()

    /**
     * A request to store an entry on the server has been submitted.
     */
    data object EntrySubmitted : MainViewEvent()

    data object BackupSuccessful : MainViewEvent()

    data object BackupRequiresPermission : MainViewEvent()

    data object BackupIoError : MainViewEvent()

    data object BackupUnsupported : MainViewEvent()

    /**
     * A request to open the date & time picker to set the starting time of an entry has been submitted.
     */
    data object PickStartedDatetimeRequest : MainViewEvent()

    /**
     * A request to open the date & time picker to set the stopped time of an entry has been submitted.
     */
    data object PickStoppedDatetimeRequest : MainViewEvent()

    /**
     * A previous request to open the date & time picker to set a starting time for an entry has been cancelled.
     */
    data object CancelledPickStartedDatetime : MainViewEvent()

    /**
     * A previous request to open the date & time picker to set a stopped time for an entry has been cancelled.
     */
    data object CancelledPickStoppedDatetime : MainViewEvent()

    /**
     * A previously submitted request to store an entry on the server has gotten an invalid credentials response.
     *
     * In normal circumstances, this indicates that the session cookie used to make requests against the server has
     * expired, and the user will need to login again.
     */
    data object InvalidCredentialsError : MainViewEvent()

    data object SslHandshakeError : MainViewEvent()

    /**
     * A network error occurred for a previously submitted request to store an entry on the server
     */
    data object NetworkError : MainViewEvent()
}
