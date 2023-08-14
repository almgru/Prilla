package com.almgru.prilla.android

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.almgru.prilla.android.helpers.Constants.LAUNCH_TIMEOUT_MS
import com.almgru.prilla.android.helpers.Constants.SHORT_TIMEOUT_MS
import com.almgru.prilla.android.helpers.MockWebServerExtensions.mockDelayedSuccessfulResponse
import com.almgru.prilla.android.helpers.MockWebServerExtensions.mockErrorResponse
import com.almgru.prilla.android.helpers.MockWebServerExtensions.mockSuccessfulResponse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUITest {
    private lateinit var device: UiDevice
    private lateinit var resName: (Int) -> String
    private lateinit var getStr: (Int) -> String

    private val mockServer = MockWebServer()
    private val baseUrl = mockServer.url("/").toString()

    @Before
    fun setup() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageName = instrumentation.targetContext.packageName

        device = UiDevice.getInstance(instrumentation)
        resName = context.resources::getResourceName
        getStr = context.resources::getString

        context.startActivity(
            context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            } ?: error("Failed to start activity")
        )

        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), LAUNCH_TIMEOUT_MS)
    }

    @Test
    fun login_press_with_correct_inputs_shows_main_view() {
        mockServer.mockSuccessfulResponse()

        device.findObject(By.res(resName(R.id.serverField))).text = baseUrl
        device.findObject(By.res(resName(R.id.usernameField))).text = "username"
        device.findObject(By.res(resName(R.id.passwordField))).text = "password"

        device.findObject(By.res(resName(R.id.loginButton))).clickAndWait(
            Until.newWindow(),
            SHORT_TIMEOUT_MS
        )

        assertNotNull(device.findObject(By.res(resName(R.id.startStopButton))))
    }

    @Test
    fun login_press_displays_spinner() {
        mockServer.mockDelayedSuccessfulResponse()

        device.findObject(By.res(resName(R.id.serverField))).text = baseUrl
        device.findObject(By.res(resName(R.id.usernameField))).text = "username"
        device.findObject(By.res(resName(R.id.passwordField))).text = "password"

        device.findObject(By.res(resName(R.id.loginButton))).click()

        device.wait(Until.hasObject(By.res(resName(R.id.loginProgressBar))), SHORT_TIMEOUT_MS)

        assertNotNull(device.findObject(By.res(resName(R.id.loginProgressBar))))
    }

    @Test
    fun login_press_with_invalid_url_shows_error() {
        device.findObject(By.res(resName(R.id.serverField))).text = "htps://example.com"
        device.findObject(By.res(resName(R.id.usernameField))).text = "username"
        device.findObject(By.res(resName(R.id.passwordField))).text = "password"

        device.findObject(By.res(resName(R.id.loginButton))).clickAndWait(
            Until.newWindow(),
            SHORT_TIMEOUT_MS
        )

        assertNotNull(device.findObject(By.text(getStr(R.string.network_error_title))))
    }

    @Test
    fun login_press_with_invalid_credentials_shows_error() {
        mockServer.mockErrorResponse()

        device.findObject(By.res(resName(R.id.serverField))).text = baseUrl
        device.findObject(By.res(resName(R.id.usernameField))).text = "username"
        device.findObject(By.res(resName(R.id.passwordField))).text = "password"

        device.findObject(By.res(resName(R.id.loginButton))).clickAndWait(
            Until.newWindow(),
            SHORT_TIMEOUT_MS
        )

        assertNotNull(device.findObject(By.text(getStr(R.string.invalid_credentials_error_title))))
    }

    @Test
    fun can_add_text_in_middle_of_fields() {
        val serverField = device.findObject(By.res(resName(R.id.serverField)))

        serverField.text = "abcdefg"

        serverField.click()

        device.pressDPadLeft()
        device.pressKeyCode(KeyEvent.KEYCODE_H)
        device.pressKeyCode(KeyEvent.KEYCODE_E)
        device.pressKeyCode(KeyEvent.KEYCODE_J)

        assertEquals("abcdefhejg", serverField.text)
    }
}
