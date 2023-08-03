package com.almgru.prilla.android.net

data class UnexpectedHttpStatusException(
    val statusCode: Int,
    val statusMessage:String
) : Exception()