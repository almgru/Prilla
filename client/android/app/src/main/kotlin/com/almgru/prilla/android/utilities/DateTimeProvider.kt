package com.almgru.prilla.android.utilities

import java.time.LocalDateTime

interface DateTimeProvider {
    fun getCurrentDateTime(): LocalDateTime
}
