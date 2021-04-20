package com.almgru.prilla.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.almgru.prilla.android.fragment.DatePickerFragment
import com.almgru.prilla.android.fragment.TimePickerFragment
import com.almgru.prilla.android.net.EntryAddedListener
import com.almgru.prilla.android.net.EntrySubmitter
import com.android.volley.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener,
    EntryAddedListener, View.OnLongClickListener {
    private enum class UIState {
        NOT_STARTED, STARTED, SUBMITTED, SELECTING_DATETIME
    }

    private lateinit var submitter: EntrySubmitter
    private lateinit var backupper: DataBackupManager

    private var startedDateTime: LocalDateTime? = null

    private lateinit var startStopButton: Button
    private lateinit var amountSlider: SeekBar
    private lateinit var amountLabel: TextView
    private lateinit var submitProgressIndicator : ProgressBar
    private lateinit var forgotLink : TextView

    private lateinit var datePicker : DatePickerFragment
    private lateinit var timePicker : TimePickerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startStopButton = findViewById(R.id.startStopButton)
        amountSlider = findViewById(R.id.amountSlider)
        amountLabel = findViewById(R.id.amountLabel)
        submitProgressIndicator = findViewById(R.id.submitProgressIndicator)
        forgotLink = findViewById(R.id.forgotLink)

        datePicker = DatePickerFragment(this::onDatePicked, this::onDateTimeDialogCancelled)
        timePicker = TimePickerFragment(this::onTimePicked, this::onDateTimeDialogCancelled)

        startStopButton.setOnLongClickListener(this)
        amountLabel.text = getString(R.string.amount_label).format(amountSlider.progress)
        amountSlider.setOnSeekBarChangeListener(this)

        submitter = EntrySubmitter(this, this)
        backupper = DataBackupManager(this)
    }

    override fun onResume() {
        super.onResume()

        startedDateTime = PersistenceManager.getStartedDateTime(this)

        if (startedDateTime != null) {
            setUiState(UIState.STARTED)
        } else {
            setUiState(UIState.NOT_STARTED)
        }

        backupper.backup()
    }

    fun onStartStopPressed(@Suppress("UNUSED_PARAMETER") view: View) {
        if (startedDateTime == null) {
            startedDateTime = LocalDateTime.now()
            PersistenceManager.putStartedDateTime(this, startedDateTime!!)
            setUiState(UIState.STARTED)
        } else {
            val stoppedDateTime = LocalDateTime.now()
            val amount = amountSlider.progress

            setUiState(UIState.SUBMITTED)

            submitter.submit(startedDateTime!!, stoppedDateTime, amount)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        amountLabel.text = getString(R.string.amount_label).format(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    private fun setUiState(state : UIState) {
        when (state) {
            UIState.NOT_STARTED -> {
                submitProgressIndicator.visibility = View.GONE
                startStopButton.visibility = View.VISIBLE
                startStopButton.text = getText(R.string.start_stop_button_start_text)
                forgotLink.visibility = View.VISIBLE
            }
            UIState.STARTED -> {
                submitProgressIndicator.visibility = View.GONE
                startStopButton.visibility = View.VISIBLE
                startStopButton.text = getText(R.string.start_stop_button_stop_text)
                forgotLink.visibility = View.GONE
            }
            UIState.SUBMITTED -> {
                submitProgressIndicator.visibility = View.VISIBLE
                startStopButton.visibility = View.GONE
                forgotLink.visibility = View.GONE
            }
            UIState.SELECTING_DATETIME -> {
                submitProgressIndicator.visibility = View.VISIBLE
                startStopButton.visibility = View.GONE
                forgotLink.visibility = View.GONE
            }
        }
    }

    override fun onEntryAdded() {
        Toast.makeText(this, "Entry added", Toast.LENGTH_SHORT).show()
        setUiState(UIState.NOT_STARTED)
        startedDateTime = null
        PersistenceManager.removeStartedDateTime(this)
    }

    override fun onEntrySubmitError(error: VolleyError) {
        Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
        setUiState(UIState.NOT_STARTED)
        startedDateTime = null
        PersistenceManager.removeStartedDateTime(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    override fun onLongClick(v: View?): Boolean {
        Toast.makeText(this, "Entry cleared", Toast.LENGTH_SHORT).show()
        setUiState(UIState.NOT_STARTED)
        startedDateTime = null
        PersistenceManager.removeStartedDateTime(this)
        return true
    }

    fun onForgotLinkPressed(@Suppress("UNUSED_PARAMETER") v : View) {
        setUiState(UIState.SELECTING_DATETIME)
        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun onDatePicked(date : LocalDate) {
        startedDateTime = LocalDateTime.of(date, LocalTime.of(0, 0, 0))
        timePicker.show(supportFragmentManager, "timePicker")
    }

    private fun onTimePicked(time : LocalTime) {
        startedDateTime = LocalDateTime.of(startedDateTime!!.toLocalDate(), time)
        PersistenceManager.putStartedDateTime(this, startedDateTime!!)
        setUiState(UIState.STARTED)
    }

    private fun onDateTimeDialogCancelled() {
        setUiState(UIState.NOT_STARTED)
    }
}