package com.almgru.prilla.android.utilities

import java.time.LocalDateTime

object LocalDateTimeProvider : DateTimeProvider {
    override fun getCurrentDateTime(): LocalDateTime = LocalDateTime.now()
}
