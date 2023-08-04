package com.almgru.prilla.android.net.exceptions

data class UnexpectedHttpStatusException(
    val statusCode: Int,
    val statusMessage: String
) : Exception()
