package se.algr.prilla.android.view

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ApiError : Parcelable {
    @Parcelize
    data object SessionExpiredError : ApiError()

    @Parcelize
    data object SslHandshakeError : ApiError()

    @Parcelize
    data object NetworkError : ApiError()
}
