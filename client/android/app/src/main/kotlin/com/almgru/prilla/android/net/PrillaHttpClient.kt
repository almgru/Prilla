package com.almgru.prilla.android.net

import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.exceptions.UnexpectedHttpStatusException
import com.almgru.prilla.android.net.results.LoginResult
import com.almgru.prilla.android.net.results.RecordEntryResult
import com.almgru.prilla.android.net.utilities.CSRFTokenExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.format.DateTimeFormatter

class PrillaHttpClient(
    private val baseUrl: String,
    private val httpClient: OkHttpClient,
    private val csrfExtractor: CSRFTokenExtractor,
) : LoginManager, EntrySubmitter {
    override suspend fun login(
        username: String, password: String
    ): LoginResult = withContext(Dispatchers.IO) {
        val csrf = getCsrfTokenFor("/")
        val loginRequest = buildPostRequest(
            "/", mapOf(
                "username" to username, "password" to password, "_csrf" to csrf
            )
        )

        return@withContext try {
            httpClient.newCall(loginRequest).execute().use {
                if (it.code != 200) {
                    throw UnexpectedHttpStatusException(it.code, it.message)
                }

                when (it.request.url.toString().endsWith("/")) {
                    true -> LoginResult.Success
                    false -> LoginResult.InvalidCredentials
                }
            }
        } catch (io: IOException) {
            LoginResult.NetworkError
        }
    }

    override suspend fun hasActiveSession() = withContext(Dispatchers.IO) {
        val getIndexRequest = Request.Builder().url("${baseUrl}/").get().build()

        return@withContext try {
            httpClient.newCall(getIndexRequest).execute().use {
                when (it.code) {
                    200 -> it.request.url.toString().endsWith("/")
                    else -> false
                }
            }
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun submit(entry: Entry): RecordEntryResult = withContext(Dispatchers.IO) {
        val csrf = getCsrfTokenFor("/record")
        val recordRequest = buildPostRequest(
            "/record", mapOf(
                "appliedDate" to DateTimeFormatter.ISO_DATE.format(entry.started.toLocalDate()),
                "appliedTime" to DateTimeFormatter.ISO_DATE.format(entry.started.toLocalTime()),
                "removedDate" to DateTimeFormatter.ISO_DATE.format(entry.stopped.toLocalDate()),
                "removedTime" to DateTimeFormatter.ISO_DATE.format(entry.stopped.toLocalTime()),
                "amount" to entry.amount.toString(),
                "_csrf" to csrf
            )
        )

        return@withContext try {
            httpClient.newCall(recordRequest).execute().use {
                if (it.code != 200) {
                    throw UnexpectedHttpStatusException(it.code, it.message)
                }

                when (it.request.url.toString().endsWith("/")) {
                    true -> RecordEntryResult.Success
                    false -> RecordEntryResult.SessionExpiredError
                }
            }
        } catch (io: IOException) {
            RecordEntryResult.NetworkError
        }
    }

    private suspend fun getCsrfTokenFor(path: String) = withContext(Dispatchers.IO) {
        val getFormRequest = Request.Builder().url("${baseUrl}/${path}").get().build()

        httpClient.newCall(getFormRequest).execute().use {
            when (it.code) {
                200 -> csrfExtractor.extractCSRFToken(it.body.toString())
                else -> throw UnexpectedHttpStatusException(it.code, it.message)
            }
        }
    }

    private fun buildPostRequest(path: String, form: Map<String, String>) =
        Request.Builder().addHeader("Content-Type", "application/x-www-form-urlencoded")
            .url("${baseUrl}/${path}").post(buildFormBody(form)).build()

    private fun buildFormBody(form: Map<String, String>): FormBody {
        val builder = FormBody.Builder()

        form.forEach {
            builder.add(it.key, it.value)
        }

        return builder.build()
    }
}