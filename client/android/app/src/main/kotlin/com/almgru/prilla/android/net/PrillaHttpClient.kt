package com.almgru.prilla.android.net

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class LoginResult {
    data object Success : LoginResult()
    data object Failure : LoginResult()
}

sealed class RecordResult {
    data object Success : RecordResult()
    data object Failure : RecordResult()
}

class PrillaHttpClient(
    private val baseUrl: String,
    private val httpClient: OkHttpClient,
    private val csrfExtractor: CsrfExtractor,
) {
    fun login(username: String, password: String): LoginResult {
        val csrf = getCsrfTokenFor("/")
        val loginRequest = buildPostRequest(
            "/", mapOf(
                "username" to username, "password" to password, "_csrf" to csrf
            )
        )

        httpClient.newCall(loginRequest).execute().use {
            if (it.code != 200) {
                throw UnexpectedHttpStatusException(it.code, it.message)
            }

            return when (it.request.url.toString().endsWith("/")) {
                true -> LoginResult.Success
                false -> LoginResult.Failure
            }
        }
    }

    fun record(start: LocalDateTime, stop: LocalDateTime, amount: Int): RecordResult {
        val csrf = getCsrfTokenFor("/record")
        val recordRequest = buildPostRequest(
            "/record", mapOf(
                "appliedDate" to DateTimeFormatter.ISO_DATE.format(start.toLocalDate()),
                "appliedTime" to DateTimeFormatter.ISO_DATE.format(start.toLocalTime()),
                "removedDate" to DateTimeFormatter.ISO_DATE.format(stop.toLocalDate()),
                "removedTime" to DateTimeFormatter.ISO_DATE.format(stop.toLocalTime()),
                "amount" to amount.toString(),
                "_csrf" to csrf
            )
        )

        httpClient.newCall(recordRequest).execute().use {
            if (it.code != 200) {
                throw UnexpectedHttpStatusException(it.code, it.message)
            }

            return when (it.request.url.toString().endsWith("/")) {
                true -> RecordResult.Success
                false -> RecordResult.Failure
            }
        }
    }

    private fun getCsrfTokenFor(path: String): String {
        val getFormRequest = Request.Builder().url("${baseUrl}/${path}").get().build()
        val getFormResponse = httpClient.newCall(getFormRequest).execute().use {
            when (it.code) {
                200 -> it.body.toString()
                else -> throw UnexpectedHttpStatusException(it.code, it.message)
            }
        }

        return csrfExtractor.extractCsrfToken(getFormResponse)
    }

    private fun buildPostRequest(path: String, form: Map<String, String>) =
        Request.Builder().addHeader("Content-Type", "application/x-www-form-urlencoded").url("${baseUrl}/${path}")
            .post(buildFormBody(form)).build()

    private fun buildFormBody(form: Map<String, String>): FormBody {
        val builder = FormBody.Builder()

        form.forEach {
            builder.add(it.key, it.value)
        }

        return builder.build()
    }
}