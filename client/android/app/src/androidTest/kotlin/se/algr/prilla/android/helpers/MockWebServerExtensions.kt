package se.algr.prilla.android.helpers

import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate

object MockWebServerExtensions {
    fun MockWebServer.mockSuccessfulLoginResponse(delayInSec: Long? = null) = this.respond(
        "/",
        delayInSec?.div(2)?.times(1000)
    )

    fun MockWebServer.mockSuccessfulRecordResponse(delayInSec: Long? = null) = this.respond(
        "/record",
        delayInSec?.div(2)?.times(1000)
    )

    fun MockWebServer.mockErrorResponse(delayInSec: Long? = null) = this.respond(
        "/error",
        delayInSec?.div(2)?.times(1000)
    )

    fun MockWebServer.mockFailedSslHandshake() {
        val localhost = HeldCertificate.Builder()
            .commonName("localhost")
            .addSubjectAlternativeName("127.0.0.1")
            .build()

        val serverCertificates = HandshakeCertificates.Builder()
            .heldCertificate(localhost)
            .build()

        useHttps(serverCertificates.sslSocketFactory(), false)

        enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.FAIL_HANDSHAKE)
        )
    }

    private fun MockWebServer.respond(redirectLocation: String, delayInMs: Long? = null) {
        enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
                .apply { delayInMs?.let { setBodyDelay(delayInMs, TimeUnit.MILLISECONDS) } }
        )
        enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setHeader("Location", redirectLocation)
                .apply { delayInMs?.let { setHeadersDelay(delayInMs, TimeUnit.MILLISECONDS) } }
        )
    }
}
