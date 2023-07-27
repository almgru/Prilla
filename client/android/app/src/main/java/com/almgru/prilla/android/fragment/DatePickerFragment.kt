package com.almgru.prilla.android.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.time.LocalDate

class DatePickerFragment(
    private var callback: (LocalDate) -> Unit,
    private var cancelCallback: () -> Unit
) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val now = LocalDate.now();

        return DatePickerDialog(
            requireActivity(),
            this,
            now.year,
            now.month.value - 1,
            now.dayOfMonth);
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        callback(LocalDate.of(year, month + 1, dayOfMonth));
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        cancelCallback()
    }
}