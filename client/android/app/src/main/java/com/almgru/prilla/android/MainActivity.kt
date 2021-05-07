package com.almgru.prilla.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.almgru.prilla.android.fragment.DatePickerFragment
import com.almgru.prilla.android.fragment.TimePickerFragment
import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.EntryAddedListener
import com.almgru.prilla.android.net.EntrySubmitter
import com.android.volley.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener,
    EntryAddedListener, View.OnLongClickListener {
    private enum class UIState {
        NOT_STARTED, STARTED, SUBMITTED, SELECTING_DATETIME
    }

    private lateinit var submitter: EntrySubmitter
    private lateinit var backupper: DataBackupManager

    private var startedDateTime: LocalDateTime? = null
    private var lastEntry: Entry? = null

    private lateinit var startStopButton: Button
    private lateinit var amountSlider: SeekBar
    private lateinit var amountLabel: TextView
    private lateinit var submitProgressIndicator: ProgressBar
    private lateinit var customStartedLink: TextView
    private lateinit var customStoppedLink: TextView
    private lateinit var lastEntryText: TextView
    private lateinit var startedAtText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startStopButton = findViewById(R.id.startStopButton)
        amountSlider = findViewById(R.id.amountSlider)
        amountLabel = findViewById(R.id.amountLabel)
        submitProgressIndicator = findViewById(R.id.submitProgressIndicator)
        customStartedLink = findViewById(R.id.forgotToStartLink)
        customStoppedLink = findViewById(R.id.forgotToStopLink)
        lastEntryText = findViewById(R.id.lastEntryText)
        startedAtText = findViewById(R.id.startedAtText)

        startStopButton.setOnLongClickListener(this)
        amountLabel.text = getString(R.string.amount_label).format(amountSlider.progress)
        amountSlider.setOnSeekBarChangeListener(this)

        submitter = EntrySubmitter(this, this)
        backupper = DataBackupManager(this)
        lastEntry = PersistenceManager.getLastEntry(this)
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

            lastEntry = Entry(startedDateTime!!, stoppedDateTime, amount)
            submitter.submit(startedDateTime!!, stoppedDateTime, amount)

            setUiState(UIState.SUBMITTED)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        amountLabel.text = getString(R.string.amount_label).format(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    private fun setUiState(state: UIState) {
        when (state) {
            UIState.NOT_STARTED -> {
                submitProgressIndicator.visibility = View.GONE
                startStopButton.visibility = View.VISIBLE
                startStopButton.text = getText(R.string.start_stop_button_start_text)
                customStartedLink.visibility = View.VISIBLE
                customStoppedLink.visibility = View.GONE
                startedAtText.visibility = View.GONE

                if (lastEntry != null) {
                    lastEntryText.visibility = View.VISIBLE
                    lastEntryText.text = getString(
                        R.string.last_entry_text,
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(lastEntry!!.started),
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(lastEntry!!.stopped)
                    )
                } else {
                    lastEntryText.visibility = View.GONE
                }
            }
            UIState.STARTED -> {
                submitProgressIndicator.visibility = View.GONE
                startStopButton.visibility = View.VISIBLE
                startStopButton.text = getText(R.string.start_stop_button_stop_text)
                customStartedLink.visibility = View.GONE
                customStoppedLink.visibility = View.VISIBLE
                startedAtText.visibility = View.VISIBLE
                startedAtText.text = getString(
                    R.string.started_at_text,
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(startedDateTime)
                )
                lastEntryText.visibility = View.GONE
            }
            UIState.SUBMITTED -> {
                submitProgressIndicator.visibility = View.VISIBLE
                startStopButton.visibility = View.GONE
                customStartedLink.visibility = View.GONE
                customStoppedLink.visibility = View.GONE
                startedAtText.visibility = View.GONE
                lastEntryText.visibility = View.GONE
            }
            UIState.SELECTING_DATETIME -> {
                submitProgressIndicator.visibility = View.VISIBLE
                startStopButton.visibility = View.GONE
                customStartedLink.visibility = View.GONE
                customStoppedLink.visibility = View.GONE
                startedAtText.visibility = View.GONE
                lastEntryText.visibility = View.GONE
            }
        }
    }

    override fun onEntryAdded() {
        Toast.makeText(this, "Entry added", Toast.LENGTH_SHORT).show()
        PersistenceManager.putLastEntry(this, lastEntry!!)
        startedDateTime = null
        PersistenceManager.removeStartedDateTime(this)
        setUiState(UIState.NOT_STARTED)
    }

    override fun onEntrySubmitError(error: VolleyError) {
        Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
        setUiState(UIState.NOT_STARTED)
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

    fun onCustomStartedPressed(@Suppress("UNUSED_PARAMETER") v: View) {
        setUiState(UIState.SELECTING_DATETIME)
        DatePickerFragment({ date ->
            TimePickerFragment({ time ->
                startedDateTime = LocalDateTime.of(date, time)
                PersistenceManager.putStartedDateTime(this, startedDateTime!!)
                setUiState(UIState.STARTED)
            }, {
                setUiState(UIState.NOT_STARTED)
            }).show(supportFragmentManager, "timePicker")
        }, {
            setUiState(UIState.NOT_STARTED)
        }).show(supportFragmentManager, "datePicker")
    }

    fun onCustomStoppedPressed(@Suppress("UNUSED_PARAMETER") v: View) {
        setUiState(UIState.SELECTING_DATETIME)
        DatePickerFragment({ date ->
            TimePickerFragment({ time ->
                val amount = amountSlider.progress
                setUiState(UIState.SUBMITTED)
                submitter.submit(startedDateTime!!, LocalDateTime.of(date, time), amount)
            }, {
                setUiState(UIState.STARTED)
            }).show(supportFragmentManager, "timePicker")
        }, {
            setUiState(UIState.STARTED)
        }).show(supportFragmentManager, "datePicker")
    }
}