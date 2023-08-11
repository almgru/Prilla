package com.almgru.prilla.android.helpers

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

object Utilities {
    fun waitForView(viewMatcher: Matcher<View>, timeoutInSec: Int = 10) {
        val endTime = System.currentTimeMillis() + timeoutInSec * 1000L
        var exception: Exception? = null

        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(400)
                Espresso.onView(viewMatcher)
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                return
            } catch (e: Exception) {
                exception = e
            }
        }

        if (exception != null) throw exception
    }
}
