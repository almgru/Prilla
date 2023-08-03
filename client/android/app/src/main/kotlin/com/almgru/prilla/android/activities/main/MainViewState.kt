package com.almgru.prilla.android.activities.main;

import com.almgru.prilla.android.model.Entry
import java.time.LocalDateTime

data class MainViewState(
    val latestEntry: Entry?, val startedDateTime: LocalDateTime?, val amount: Int
)