package com.almgru.prilla.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.cookie.SerializableHttpCookie
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.net.HttpCookie
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

class PersistenceManager(private val context: Context) {
    fun getServerUrl(): String? {
        return readString(R.string.shared_prefs_server_url_key)
    }

    fun putServerUrl(url: String) {
        writeString(R.string.shared_prefs_server_url_key, url)
    }

    fun getAuthCookie(): HttpCookie? {
        val raw = readString(R.string.shared_prefs_cookie_auth_key) ?: return null

        return Json.decodeFromString<SerializableHttpCookie>(raw).toHttpCookie()
    }

    fun putAuthCookie(cookie: HttpCookie) {
        val httpOnly = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            cookie.isHttpOnly
        else
            false

        val serializable = SerializableHttpCookie(
            cookie.comment, cookie.commentURL, cookie.discard, cookie.domain, cookie.maxAge,
            cookie.name, cookie.path, cookie.portlist, cookie.secure, cookie.value, cookie.version,
            httpOnly
        )

        writeString(R.string.shared_prefs_cookie_auth_key, Json.encodeToString(serializable))
    }

    fun removeAuthCookie() {
        remove(R.string.shared_prefs_cookie_auth_key)
    }

    fun getStartedDateTime(): LocalDateTime? {
        val date = readString(R.string.shared_prefs_started_key) ?: return null
        return LocalDateTime.parse(date)
    }

    fun putStartedDateTime(dt: LocalDateTime) {
        writeString(R.string.shared_prefs_started_key, DateTimeFormatter.ISO_DATE_TIME.format(dt))
    }

    fun removeStartedDateTime() {
        remove(R.string.shared_prefs_started_key)
    }

    fun getLastUpdateTimestamp(): LocalDateTime? {
        return try {
            LocalDateTime.parse(readString(R.string.shared_prefs_last_backup_timestamp))
        } catch(e : Exception) {
            null
        }
    }

    fun putLastUpdateTimestamp(timestamp : LocalDateTime) {
        writeString(R.string.shared_prefs_last_backup_timestamp, timestamp.toString())
    }

    fun getUpdateInterval() : Period? {
        return try {
            Period.parse(readString(R.string.shared_prefs_update_interval))
        } catch (ex : Exception) {
            null
        }
    }

    fun putUpdateInterval(updateInterval: Period) {
        writeString(R.string.shared_prefs_update_interval, updateInterval.toString())
    }

    fun putLastEntry(entry : Entry) {
        writeString(R.string.shared_prefs_last_entry, Json.encodeToString(entry))
    }

    fun getLastEntry() : Entry? {
        val raw = readString(R.string.shared_prefs_last_entry) ?: return null

        return Json.decodeFromString(raw)
    }

    private fun writeString(keyId: Int, value: String) {
        with(getDefaultSharedPrefs().edit()) {
            putString(context.getString(keyId), value)
            apply()
        }
    }

    private fun readString(keyId: Int): String? {
        return getDefaultSharedPrefs().getString(context.getString(keyId), null)
    }

    private fun remove(keyId: Int) {
        with(getDefaultSharedPrefs().edit()) {
            remove(context.getString(keyId))
            apply()
        }
    }

    private fun getDefaultSharedPrefs(): SharedPreferences {
        return context.getSharedPreferences(
            context.getString(R.string.shared_prefs),
            Context.MODE_PRIVATE
        )
    }
}