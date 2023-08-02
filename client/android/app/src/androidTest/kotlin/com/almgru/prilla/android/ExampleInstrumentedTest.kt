package com.almgru.prilla.android

import android.view.View
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.almgru.prilla.android.activities.login.LoginActivity
import org.hamcrest.Matcher
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

fun waitForView(viewMatcher: Matcher<View>, timeout: Long = 10000) {
    val endTime = System.currentTimeMillis() + timeout
    var exception: Exception? = null

    while (System.currentTimeMillis() < endTime) {
        try {
            Thread.sleep(400)
            onView(viewMatcher).check(matches(isDisplayed()))
            return
        } catch (e: Exception) {
            exception = e
        }
    }

    if (exception != null) throw exception
}

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() { // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.almgru.prilla.android", appContext.packageName)
    }

    @Test
    fun startThenStopAddsEntry() {
        val serverHost = System.getenv("PRILLA_SERVER_URL_TEST")
        val username = System.getenv("PRILLA_USERNAME_TEST")
        val password = System.getenv("PRILLA_PASSWORD_TEST")

        launch(LoginActivity::class.java).use {
            onView(withId(R.id.serverField)).perform(
                click(),
                replaceText(serverHost),
                closeSoftKeyboard()
            )
            onView(withId(R.id.usernameField)).perform(
                click(),
                replaceText(username),
                closeSoftKeyboard()
            )
            onView(withId(R.id.passwordField)).perform(
                click(),
                replaceText(password),
                closeSoftKeyboard()
            )
            onView(withId(R.id.loginButton)).perform(click())

            waitForView(withId(R.id.startStopButton))

            onView(withId(R.id.startStopButton)).check(matches(withSubstring("Start")))
            onView(withId(R.id.startStopButton)).perform(click())
            onView(withId(R.id.startStopButton)).check(matches(withSubstring("Stop")))
            onView(withId(R.id.startStopButton)).perform(click())

            waitForView(withId(R.id.lastEntryText))

            onView(withId(R.id.startStopButton)).check(matches(withSubstring("Start")))
        }
    }
}