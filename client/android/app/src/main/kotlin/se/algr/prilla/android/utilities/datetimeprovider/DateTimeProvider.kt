package se.algr.prilla.android.utilities.datetimeprovider

import java.time.LocalDateTime

interface DateTimeProvider {
    fun getCurrentDateTime(): LocalDateTime
}
