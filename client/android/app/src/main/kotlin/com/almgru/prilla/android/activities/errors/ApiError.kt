package com.almgru.prilla.android.activities.errors

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ApiError : Parcelable {
    @Parcelize
    data object NetworkError : ApiError()

    @Parcelize
    data object SessionExpiredError : ApiError()
}
