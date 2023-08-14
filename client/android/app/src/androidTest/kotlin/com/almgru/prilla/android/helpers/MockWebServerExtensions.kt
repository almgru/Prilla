package com.almgru.prilla.android.helpers

import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

object MockWebServerExtensions {
    fun MockWebServer.mockSuccessfulResponse() {
        enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setHeader("Location", "/")
        )
    }

    fun MockWebServer.mockDelayedSuccessfulResponse(delayInSec: Long = 2L) {
        enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
                .setBodyDelay(delayInSec, TimeUnit.SECONDS)
        )
        enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setHeader("Location", "/")
                .setHeadersDelay(delayInSec, TimeUnit.SECONDS)
        )
    }

    fun MockWebServer.mockErrorResponse() {
        enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setHeader("Location", "/error")
        )
    }
}
