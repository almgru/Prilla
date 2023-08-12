package com.almgru.prilla.android.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ErrorDialogFragment(
    private val message: String
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Okay") { _, _ -> dismiss() }

            builder.create()
        } ?: error("Activity cannot be null.")
    }
}
