package com.almgru.prilla.android.fragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.time.LocalTime

class TimePickerFragment(
    private var callback: (LocalTime) -> Unit,
    private var cancelCallback: () -> Unit
) : DialogFragment(),
    TimePickerDialog.OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val now = LocalTime.now()

        return TimePickerDialog(
            activity,
            this,
            now.hour,
            now.minute,
            android.text.format.DateFormat.is24HourFormat(activity)
        )
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        callback(LocalTime.of(hourOfDay, minute));
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        cancelCallback()
    }
}