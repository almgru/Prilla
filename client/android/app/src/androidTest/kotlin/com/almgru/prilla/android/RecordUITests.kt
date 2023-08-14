package com.almgru.prilla.android

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.almgru.prilla.android.helpers.MockWebServerExtensions.mockSuccessfulServerResponse
import com.almgru.prilla.android.helpers.UiDeviceExtensions.authenticate
import junit.framework.TestCase.assertNotNull
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test

private const val LAUNCH_TIMEOUT_MS = 5000L
private const val SHORT_TIMEOUT_MS = 1000L

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
    }

    @Test
    fun starts_at_main_view() {
        device.wait(Until.hasObject(By.res(resName(R.id.startStopButton))), SHORT_TIMEOUT_MS)
        assertNotNull(device.findObject(By.res(resName(R.id.startStopButton))))
    }

    @Test
    fun record_entry_adds_last_entry_text() {
        device.wait(Until.hasObject(By.res(resName(R.id.startStopButton))), SHORT_TIMEOUT_MS)

        val button = device.findObject(By.res(resName(R.id.startStopButton)))

        button.click()

        mockServer.mockSuccessfulServerResponse()

        device.wait(
            Until.gone(By.res(resName(R.id.submitProgressIndicator))),
            SHORT_TIMEOUT_MS
        )

        button.click()

        device.wait(
            Until.gone(By.res(resName(R.id.submitProgressIndicator))),
            SHORT_TIMEOUT_MS
        )

        assertNotNull(device.findObject(By.res(resName(R.id.lastEntryText))))
    }
}
