package com.almgru.snustrack.android.net

import com.android.volley.VolleyError

interface EntryAddedListener {
    fun onEntryAdded()
    fun onEntrySubmitError(error : VolleyError)
}
