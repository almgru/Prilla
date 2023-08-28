package se.algr.prilla.android.helpers

import java.time.Duration
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

private const val DEFAULT_CLIENT_READ_TIMEOUT_SEC = 10L

class CustomClientReadTimeoutRule : TestRule {
    var timeout: Duration = Duration.ofSeconds(DEFAULT_CLIENT_READ_TIMEOUT_SEC)

    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                description
                    ?.getAnnotation(CustomOkHttpClientReadTimeout::class.java)
                    ?.timeoutMillis
                    ?.let { timeout = Duration.ofMillis(it) }

                base?.evaluate()
            }
        }
    }
}
