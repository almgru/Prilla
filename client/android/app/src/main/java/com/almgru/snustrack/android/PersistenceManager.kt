package com.almgru.snustrack.android

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PersistenceManager {
    fun getServerUrl(context: Context): String? {
        return readString(context, R.string.storage_prefs, R.string.storage_prefs_server_url_key)
    }

    fun putServerUrl(context: Context, url : String) {
        writeString(context, R.string.storage_prefs, R.string.storage_prefs_server_url_key, url)
    }

    fun getAuthCookie(context: Context): String? {
        return readString(context, R.string.storage_cookies, R.string.storage_cookies_auth_key)
    }

    fun putAuthCookie(context: Context, cookie: String) {
        writeString(context, R.string.storage_cookies, R.string.storage_cookies_auth_key, cookie)
    }

    fun removeAuthCookie(context: Context) {
        remove(context, R.string.storage_cookies, R.string.storage_cookies_auth_key)
    }

    fun getStartedDateTime(context: Context): LocalDateTime? {
        val date = readString(context, R.string.storage_state, R.string.storage_state_started_key)
            ?: return null

        return LocalDateTime.parse(date)
    }

    fun putStartedDateTime(context: Context, dt: LocalDateTime) {
        writeString(
            context,
            R.string.storage_state,
            R.string.storage_state_started_key,
            DateTimeFormatter.ISO_DATE_TIME.format(dt)
        )
    }

    fun removeStartedDateTime(context: Context) {
        remove(context, R.string.storage_state, R.string.storage_state_started_key)
    }

    private fun writeString(context: Context, nameId: Int, keyId: Int, value: String) {
        with(getPreferences(context, nameId).edit()) {
            putString(context.getString(keyId), value)
            apply()
        }
    }

    private fun readString(context: Context, nameId: Int, keyId: Int): String? {
        return getPreferences(context, nameId).getString(context.getString(keyId), null)
    }

    private fun remove(context: Context, nameId: Int, keyId: Int) {
        with(getPreferences(context, nameId).edit()) {
            remove(context.getString(keyId))
            apply()
        }
    }

    private fun getPreferences(context: Context, nameId: Int): SharedPreferences {
        return context.getSharedPreferences(context.getString(nameId), Context.MODE_PRIVATE)
    }
}