package com.almgru.prilla.android

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertNotNull
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val LAUNCH_TIMEOUT_MS = 5000L

@RunWith(AndroidJUnit4::class)
class LoginUITest {
    private lateinit var device: UiDevice
    private lateinit var resName: (Int) -> String // alias for context.resources.getResourceName

    private val mockServer = MockWebServer()
    private val baseUrl = mockServer.url("/")

    @Before
    fun setup() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageName = instrumentation.targetContext.packageName

        device = UiDevice.getInstance(instrumentation)
        resName = context.resources::getResourceName

        context.startActivity(
            context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            } ?: error("Failed to start activity")
        )

        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), LAUNCH_TIMEOUT_MS)
    }

    @Test
    fun login_press_with_correct_inputs_shows_main_view() {
        val loginTimeoutMs = 500L

        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setHeader("Location", "/")
        )

        device.findObject(By.res(resName(R.id.serverField))).text = baseUrl.toString()
        device.findObject(By.res(resName(R.id.usernameField))).text = "username"
        device.findObject(By.res(resName(R.id.passwordField))).text = "password"

        device.findObject(By.res(resName(R.id.loginButton))).clickAndWait(
            Until.newWindow(),
            loginTimeoutMs
        )

        assertNotNull(device.findObject(By.res(resName(R.id.startStopButton))))
    }

    @Test
    fun login_press_should_display_spinner() {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
                .setBodyDelay(2, TimeUnit.SECONDS)
        )
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setHeader("Location", "/")
                .setHeadersDelay(2, TimeUnit.SECONDS)
        )

        device.findObject(By.res(resName(R.id.serverField))).text = baseUrl.toString()
        device.findObject(By.res(resName(R.id.usernameField))).text = "username"
        device.findObject(By.res(resName(R.id.passwordField))).text = "password"

        device.findObject(By.res(resName(R.id.loginButton))).click()

        assertNotNull(device.findObject(By.res(resName(R.id.loginProgressBar))))
    }
}
