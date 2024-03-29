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

object PersistenceManager {
    fun getServerUrl(context: Context): String? {
        return readString(context, R.string.shared_prefs_server_url_key)
    }

    fun putServerUrl(context: Context, url: String) {
        writeString(context, R.string.shared_prefs_server_url_key, url)
    }

    fun getAuthCookie(context: Context): HttpCookie? {
        val raw = readString(context, R.string.shared_prefs_cookie_auth_key) ?: return null

        return Json.decodeFromString<SerializableHttpCookie>(raw).toHttpCookie()
    }

    fun putAuthCookie(context: Context, cookie: HttpCookie) {
        val httpOnly = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            cookie.isHttpOnly
        else
            false

        val serializable = SerializableHttpCookie(
            cookie.comment, cookie.commentURL, cookie.discard, cookie.domain, cookie.maxAge,
            cookie.name, cookie.path, cookie.portlist, cookie.secure, cookie.value, cookie.version,
            httpOnly
        )

        writeString(context, R.string.shared_prefs_cookie_auth_key, Json.encodeToString(serializable))
    }

    fun removeAuthCookie(context: Context) {
        remove(context, R.string.shared_prefs_cookie_auth_key)
    }

    fun getStartedDateTime(context: Context): LocalDateTime? {
        val date = readString(context, R.string.shared_prefs_started_key) ?: return null
        return LocalDateTime.parse(date)
    }

    fun putStartedDateTime(context: Context, dt: LocalDateTime) {
        writeString(
            context, R.string.shared_prefs_started_key, DateTimeFormatter.ISO_DATE_TIME.format(dt)
        )
    }

    fun removeStartedDateTime(context: Context) {
        remove(context, R.string.shared_prefs_started_key)
    }

    fun getLastUpdateTimestamp(context: Context): LocalDateTime? {
        return try {
            LocalDateTime.parse(readString(context, R.string.shared_prefs_last_backup_timestamp))
        } catch(e : Exception) {
            null
        }
    }

    fun putLastUpdateTimestamp(context: Context, timestamp : LocalDateTime) {
        writeString(context, R.string.shared_prefs_last_backup_timestamp, timestamp.toString())
    }

    fun getUpdateInterval(context: Context) : Period? {
        return try {
            Period.parse(readString(context, R.string.shared_prefs_update_interval))
        } catch (ex : Exception) {
            null
        }
    }

    fun putUpdateInterval(context: Context, updateInterval: Period) {
        writeString(context, R.string.shared_prefs_update_interval, updateInterval.toString())
    }

    fun putLastEntry(context: Context, entry : Entry) {
        writeString(context, R.string.shared_prefs_last_entry, Json.encodeToString(entry))
    }

    fun getLastEntry(context: Context) : Entry? {
        val raw = readString(context, R.string.shared_prefs_last_entry) ?: return null

        return Json.decodeFromString(raw)
    }

    private fun writeString(context: Context, keyId: Int, value: String) {
        with(getDefaultSharedPrefs(context).edit()) {
            putString(context.getString(keyId), value)
            apply()
        }
    }

    private fun readString(context: Context, keyId: Int): String? {
        return getDefaultSharedPrefs(context).getString(context.getString(keyId), null)
    }

    private fun remove(context: Context, keyId: Int) {
        with(getDefaultSharedPrefs(context).edit()) {
            remove(context.getString(keyId))
            apply()
        }
    }

    private fun getDefaultSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            context.getString(R.string.shared_prefs),
            Context.MODE_PRIVATE
        )
    }
}