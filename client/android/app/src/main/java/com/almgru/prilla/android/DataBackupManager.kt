package com.almgru.prilla.android

import android.content.Context
import com.android.volley.Request.Method.GET
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DataBackupManager(private val context: Context) {
    private val queue = Volley.newRequestQueue(context)

    fun backup() {
        if (shouldPerformBackup(context)) {
            val serverUrl = PersistenceManager.getServerUrl(context)
            val endpoint = context.getString(R.string.server_backup_endpoint)
            val url = "${serverUrl}${endpoint}"

            queue.add(
                JsonArrayRequest(
                    GET, url, null, Response.Listener(this::onResponse),
                    Response.ErrorListener(this::onError)
                )
            )
        }
    }

    private fun shouldPerformBackup(context: Context): Boolean {
        val lastUpdatedTimestamp = PersistenceManager.getLastUpdateTimestamp(context) ?: return true
        val updateInterval = PersistenceManager.getUpdateInterval(context) ?: return false
        val now = LocalDateTime.now()

        return now.isAfter(lastUpdatedTimestamp.plus(updateInterval))
    }

    private fun onResponse(response: JSONArray) {
        val now = LocalDateTime.now()
        val appendix = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(now)
        val dir = context.getExternalFilesDir(context.getString(R.string.file_storage_backup))
        val filename = "backup-${appendix}.json"

        Thread(Runnable {
            File(dir, filename).writeText(response.toString(2))
        }).start()

        PersistenceManager.putLastUpdateTimestamp(context, now)
    }

    private fun onError(error: VolleyError) {
        println(error)
    }
}