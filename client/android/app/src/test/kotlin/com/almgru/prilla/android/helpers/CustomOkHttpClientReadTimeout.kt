package com.almgru.prilla.android.helpers

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CustomOkHttpClientReadTimeout(val timeoutMillis: Long)
