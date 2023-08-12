package com.almgru.prilla.android.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ErrorDialogFragment(
    private val titleId: Int,
    private val messageId: Int
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder
                .setTitle(titleId)
                .setMessage(messageId)
                .setCancelable(false)
                .setPositiveButton("Confirm") { _, _ -> dismiss() }

            builder.create()
        } ?: error("Activity cannot be null.")
    }
}
