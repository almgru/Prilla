package com.almgru.prilla.android.net

import androidx.datastore.core.DataStore
import com.almgru.prilla.android.ProtoSettings
import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.exceptions.UnexpectedHttpStatusException
import com.almgru.prilla.android.net.results.LoginResult
import com.almgru.prilla.android.net.results.RecordEntryResult
import com.almgru.prilla.android.net.utilities.csrf.CsrfTokenExtractor
import java.io.IOException
import java.net.HttpURLConnection
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class PrillaHttpClient @Inject constructor(
    private val httpClient: OkHttpClient,
    private val csrfExtractor: CsrfTokenExtractor,
    private val settings: DataStore<ProtoSettings>
) : LoginManager, EntrySubmitter {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var baseUrl: CompletableDeferred<String> = CompletableDeferred()

    init {
        scope.launch {
            settings.data.collect {
                if (it.serverUrl.isNullOrEmpty()) return@collect

                if (baseUrl.isCompleted) { baseUrl = CompletableDeferred() }
                baseUrl.complete(it.serverUrl)
            }
        }
    }

    override suspend fun login(
        username: String,
        password: String
    ): LoginResult = withContext(Dispatchers.IO) {
        val url = Request.Builder().url("${baseUrl.await()}/login").build().url
        httpClient.cookieJar.saveFromResponse(url, emptyList())

        val csrf = getCsrfTokenFor(url)
        val loginRequest = buildPostRequest(
            url,
            mapOf(
                "username" to username,
                "password" to password,
                "_csrf" to csrf
            )
        )

        return@withContext try {
            httpClient.newCall(loginRequest).execute().use {
                when (it.code) {
                    HttpURLConnection.HTTP_MOVED_TEMP -> LoginResult.Success
                    HttpURLConnection.HTTP_UNAUTHORIZED -> LoginResult.InvalidCredentials
                    else -> throw UnexpectedHttpStatusException(it.code, it.message)
                }
            }
        } catch (io: IOException) {
            LoginResult.NetworkError(io)
        }
    }

    @Suppress("SwallowedException")
    override suspend fun hasActiveSession() = withContext(Dispatchers.IO) {
        if (!baseUrl.isCompleted) return@withContext false

        val getIndexRequest = Request.Builder().url("${baseUrl.await()}/").get().build()

        return@withContext try {
            httpClient.newCall(getIndexRequest).execute().use {
                when (it.code) {
                    HttpURLConnection.HTTP_OK -> true
                    else -> false
                }
            }
        } catch (ex: IOException) {
            false
        } catch (ex: IllegalArgumentException) {
            false
        }
    }

    override suspend fun submit(entry: Entry): RecordEntryResult = withContext(Dispatchers.IO) {
        val url = Request.Builder().url("${baseUrl.await()}/record").build().url
        val csrf = getCsrfTokenFor(url)
        val recordRequest = buildPostRequest(
            url,
            mapOf(
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
                when (it.code) {
                    HttpURLConnection.HTTP_OK -> {
                        if (it.request.url.toString().endsWith("/")) {
                            RecordEntryResult.Success
                        } else {
                            RecordEntryResult.SessionExpiredError
                        }
                    }
                    HttpURLConnection.HTTP_UNAUTHORIZED -> RecordEntryResult.SessionExpiredError
                    else -> throw UnexpectedHttpStatusException(it.code, it.message)
                }
            }
        } catch (io: IOException) {
            RecordEntryResult.NetworkError(io)
        }
    }

    private suspend fun getCsrfTokenFor(url: HttpUrl) = withContext(Dispatchers.IO) {
        val getFormRequest = Request.Builder().url(url).get().build()

        httpClient.newCall(getFormRequest).execute().use {
            when (it.code) {
                HttpURLConnection.HTTP_OK ->
                    csrfExtractor.extractCsrfToken(it.body?.string() ?: "")
                else -> throw UnexpectedHttpStatusException(it.code, it.message)
            }
        }
    }

    private fun buildPostRequest(url: HttpUrl, form: Map<String, String>) =
        Request.Builder().addHeader("Content-Type", "application/x-www-form-urlencoded")
            .url(url).post(buildFormBody(form)).build()

    private fun buildFormBody(form: Map<String, String>): FormBody {
        val builder = FormBody.Builder()

        form.forEach {
            builder.add(it.key, it.value)
        }

        return builder.build()
    }
}
