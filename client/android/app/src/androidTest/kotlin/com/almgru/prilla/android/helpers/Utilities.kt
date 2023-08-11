package com.almgru.prilla.android.helpers

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

object Utilities {
    fun waitForView(viewMatcher: Matcher<View>, delayMs: Long = 50, timeoutInSec: Int = 10) {
        val endTime = System.currentTimeMillis() + timeoutInSec * 1000

        do {
            try {
                onView(viewMatcher).check(matches(ViewMatchers.isDisplayed()))
                return
            } catch (_: Exception) { }

            Thread.sleep(delayMs)
        } while (System.currentTimeMillis() < endTime)

        error("Timeout ran out while waiting for view to be displayed")
    }
}
