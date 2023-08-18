package se.algr.prilla.android

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import se.algr.prilla.android.helpers.Constants.LAUNCH_TIMEOUT_MS
import se.algr.prilla.android.helpers.Constants.SHORT_TIMEOUT_MS
import se.algr.prilla.android.helpers.MockWebServerExtensions.mockErrorResponse
import se.algr.prilla.android.helpers.MockWebServerExtensions.mockSuccessfulRecordResponse
import se.algr.prilla.android.helpers.UiDeviceExtensions.authenticate

class RecordUITests {
    private lateinit var device: UiDevice
    private lateinit var resName: (Int) -> String
    private lateinit var getStr: (Int) -> String

    private val mockServer = MockWebServer()

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

        device.authenticate(context, mockServer)

        device.wait(Until.hasObject(By.res(resName(R.id.startStopButton))), SHORT_TIMEOUT_MS)
    }

    @Test
    fun starts_at_main_view() {
        assertNotNull(device.findObject(By.res(resName(R.id.startStopButton))))
    }

    @Test
    fun record_entry_adds_last_entry_text() {
        val button = device.findObject(By.res(resName(R.id.startStopButton)))

        button.click()

        device.wait(
            Until.hasObject(By.text(getStr(R.string.start_stop_button_stop_text))),
            SHORT_TIMEOUT_MS
        )

        mockServer.mockSuccessfulRecordResponse()

        button.click()

        device.wait(
            Until.gone(By.res(resName(R.id.submitProgressIndicator))),
            SHORT_TIMEOUT_MS
        )

        assertNotNull(device.findObject(By.res(resName(R.id.lastEntryText))))
    }

    @Test
    fun returns_to_login_view_and_shows_error_on_session_expired() {
        val button = device.findObject(By.res(resName(R.id.startStopButton)))

        button.click()

        device.wait(
            Until.hasObject(By.text(getStr(R.string.start_stop_button_stop_text))),
            SHORT_TIMEOUT_MS
        )

        mockServer.mockErrorResponse()

        button.clickAndWait(
            Until.newWindow(),
            SHORT_TIMEOUT_MS
        )

        device.wait(
            Until.hasObject(By.text(getStr(R.string.session_expired_error_message))),
            SHORT_TIMEOUT_MS
        )

        assertNotNull(device.findObject(By.text(getStr(R.string.session_expired_error_message))))
    }
}
