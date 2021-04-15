package com.almgru.snustrack.android.net

import android.content.Context
import com.almgru.snustrack.android.R
import com.almgru.snustrack.android.net.request.FormPostRequest
import com.android.volley.Request.Method.GET
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EntrySubmitter(private var context: Context, private var listener: EntryAddedListener) {
    private var queue: RequestQueue

    init {
        CookieStorage.load(context)
        queue = Volley.newRequestQueue(context)
    }

    fun submit(appliedAt: LocalDateTime, removedAt: LocalDateTime, amount: Int) {
        val serverUrl = context.getString(R.string.server_url)
        val endpoint = context.getString(R.string.server_add_entry_endpoint)
        val url = "${serverUrl}${endpoint}"

        queue.add(
            StringRequest(
                GET, url, Response.Listener<String> { response ->
                    onGetAddEntryFormResponse(response, appliedAt, removedAt, amount)
                },
                Response.ErrorListener(listener::onEntrySubmitError)
            )
        )
    }


    private fun onGetAddEntryFormResponse(
        response: String,
        started: LocalDateTime,
        stopped: LocalDateTime,
        amount: Int
    ) {
        val serverUrl = context.getString(R.string.server_url)
        val endpoint = context.getString(R.string.server_add_entry_endpoint)
        val url = "${serverUrl}${endpoint}"
        val csrfToken = CsrfExtractor.extractCsrfToken(response)
        val appliedAtDate = DateTimeFormatter.ISO_DATE.format(started.toLocalDate())
        val appliedAtTime = DateTimeFormatter.ofPattern("HH:mm").format(started.toLocalTime())
        val removedAtDate = DateTimeFormatter.ISO_DATE.format(stopped.toLocalDate())
        val removedAtTime = DateTimeFormatter.ofPattern("HH:mm").format(stopped.toLocalTime())

        queue.add(
            FormPostRequest(
                url, mapOf(
                    "appliedDate" to appliedAtDate,
                    "appliedTime" to appliedAtTime,
                    "removedDate" to removedAtDate,
                    "removedTime" to removedAtTime,
                    "amount" to amount.toString(),
                    "_csrf" to csrfToken
                ), this::onSuccess, listener::onEntrySubmitError
            )
        )
    }

    private fun onSuccess(response : String) {
        listener.onEntryAdded()
    }
}