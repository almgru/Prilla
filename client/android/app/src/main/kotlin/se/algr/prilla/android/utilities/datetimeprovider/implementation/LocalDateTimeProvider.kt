package se.algr.prilla.android.utilities.datetimeprovider.implementation

import java.time.LocalDateTime
import se.algr.prilla.android.utilities.datetimeprovider.DateTimeProvider

object LocalDateTimeProvider : DateTimeProvider {
    override fun getCurrentDateTime(): LocalDateTime = LocalDateTime.now()
}
