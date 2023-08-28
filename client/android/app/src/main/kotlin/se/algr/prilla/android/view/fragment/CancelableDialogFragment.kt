package se.algr.prilla.android.view.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class CancelableDialogFragment(
    private val titleId: Int,
    private val messageId: Int,
    private val confirmId: Int,
    private val cancelId: Int,
    private val callback: () -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder
                .setTitle(titleId)
                .setMessage(messageId)
                .setNegativeButton(cancelId) { _, _ -> dismiss() }
                .setPositiveButton(confirmId) { _, _ -> callback() }

            builder.create()
        } ?: error("Activity cannot be null.")
    }
}
