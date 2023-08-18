package se.algr.prilla.android.view.main.state

import java.time.LocalDateTime
import se.algr.prilla.android.model.CompleteEntry

/**
 * View state for the main view.
 *
 * The view state includes things like user input and data that should be presented. The state is
 * managed by [MainViewState] and consumed by [MainActivity]]
 *
 * @property latestEntry        The most recently recorded entry.
 * @property startedDateTime    Keeps track of the applied at date & time for the currently started
 *                              entry.
 * @property amount             The number of portions used for the current entry.
 */
data class MainViewState(
    val latestEntry: CompleteEntry?,
    val startedDateTime: LocalDateTime?,
    val amount: Int
)
