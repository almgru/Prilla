package com.almgru.prilla.android

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.almgru.prilla.android.activities.login.LoginActivity
import com.almgru.prilla.android.helpers.Utilities.waitForView
import java.net.HttpURLConnection
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    @get:Rule val activityScenarioRule = activityScenarioRule<LoginActivity>()

    private val mockServer = MockWebServer()
    private val baseUrl = mockServer.url("/")

    @Test
    fun login_press_with_correct_inputs_shows_main_view() {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("<input name='_csrf' value='csrf_token'>")
        )
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP))

        onView(withId(R.id.serverField)).check(matches(isDisplayed()))
        onView(withId(R.id.usernameField)).check(matches(isDisplayed()))
        onView(withId(R.id.passwordField)).check(matches(isDisplayed()))

        enterTextIntoField(R.id.serverField, baseUrl.toString())
        enterTextIntoField(R.id.usernameField, "username")
        enterTextIntoField(R.id.passwordField, "password")

        onView(withId(R.id.loginButton)).perform(ViewActions.click())

        onView(withId(R.id.loginProgressBar)).check(matches(isDisplayed()))

        waitForView(withId(R.id.startStopButton))
    }

    private fun enterTextIntoField(fieldId: Int, text: String) {
        onView(withId(fieldId)).perform(
            ViewActions.click(),
            ViewActions.replaceText(text),
            ViewActions.closeSoftKeyboard()
        )
    }
}
