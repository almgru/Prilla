package se.algr.prilla.android.helpers

import android.content.Context
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import se.algr.prilla.android.R
import okhttp3.mockwebserver.MockWebServer
import se.algr.prilla.android.helpers.MockWebServerExtensions.mockSuccessfulLoginResponse

object UiDeviceExtensions {
    fun UiDevice.authenticate(context: Context, server: MockWebServer) {
        val baseUrl = server.url("/").toString()

        server.mockSuccessfulLoginResponse()

        findObject(By.res(context.resources.getResourceName(R.id.serverField))).text = baseUrl
        findObject(By.res(context.resources.getResourceName(R.id.usernameField))).text = "username"
        findObject(By.res(context.resources.getResourceName(R.id.passwordField))).text = "password"

        findObject(By.res(context.resources.getResourceName(R.id.loginButton))).clickAndWait(
            Until.newWindow(),
            5000L
        )
    }
}