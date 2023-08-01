package com.almgru.prilla.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import com.almgru.prilla.android.fragment.DatePickerFragment
import com.almgru.prilla.android.fragment.TimePickerFragment
import com.almgru.prilla.android.model.Entry
import com.almgru.prilla.android.net.EntryAddedListener
import com.almgru.prilla.android.net.EntrySubmitter
import com.android.volley.VolleyError
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity(), EntryAddedListener {
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

        startStopButton.setOnClickListener(::onStartStopPressed)
        startStopButton.setOnLongClickListener(::onStartStopLongPressed)

        amountSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                onAmountChanged(progress = p1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })

        customStartedLink.setOnClickListener(::onCustomStartedPressed)
        customStoppedLink.setOnClickListener(::onCustomStoppedPressed)

        amountLabel.text = getString(R.string.amount_label).format(amountSlider.progress)

        submitter = EntrySubmitter(this, this)
        backupper = DataBackupManager(this)
        lastEntry = PersistenceManager.getLastEntry(this)
    }

    override fun onResume() {
        super.onResume()

        startedDateTime = PersistenceManager.getStartedDateTime(this)
        setUiState(if (startedDateTime != null) { UIState.STARTED } else { UIState.NOT_STARTED })

        backupper.backup()
    }

    override fun onEntryAdded() {
        Toast.makeText(this, "Entry added", Toast.LENGTH_SHORT).show()

        lastEntry?.let {
            PersistenceManager.putLastEntry(this, it)
        }

        handleClear()
    }

    override fun onEntrySubmitError(error: VolleyError) {
        Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private fun onStartStopPressed(@Suppress("UNUSED_PARAMETER") view: View) {
        startedDateTime?.let { handleStop(it) } ?: run { handleStart() }
    }

    private fun onAmountChanged(progress: Int) {
        amountLabel.text = getString(R.string.amount_label).format(progress)
    }

    private fun onStartStopLongPressed(@Suppress("UNUSED_PARAMETER") v: View?): Boolean {
        Toast.makeText(this, "Entry cleared", Toast.LENGTH_SHORT).show()
        handleClear()
        return true
    }

    private fun onCustomStartedPressed(@Suppress("UNUSED_PARAMETER") v: View) {
        setUiState(UIState.SELECTING_DATETIME)

        DatePickerFragment({ date ->
            TimePickerFragment({ time ->
                handleStart(LocalDateTime.of(date, time))
            }, {
                setUiState(UIState.NOT_STARTED)
            }).show(supportFragmentManager, "timePicker")
        }, {
            setUiState(UIState.NOT_STARTED)
        }).show(supportFragmentManager, "datePicker")
    }

    private fun onCustomStoppedPressed(@Suppress("UNUSED_PARAMETER") v: View) {
        startedDateTime?.let { start ->
            setUiState(UIState.SELECTING_DATETIME)

            DatePickerFragment({ date ->
                TimePickerFragment({ time ->
                    handleStop(start, LocalDateTime.of(date, time))
                }, {
                    setUiState(UIState.STARTED)
                }).show(supportFragmentManager, "timePicker")
            }, {
                setUiState(UIState.STARTED)
            }).show(supportFragmentManager, "datePicker")
        } ?: run { handleClear() }
    }

    private fun handleStart(started: LocalDateTime = LocalDateTime.now()) {
        PersistenceManager.putStartedDateTime(this, started)
        startedDateTime = started
        setUiState(UIState.STARTED)
    }

    private fun handleStop(started: LocalDateTime, stopped: LocalDateTime = LocalDateTime.now()) {
        val amount = amountSlider.progress

        lastEntry = Entry(
            started.toKotlinLocalDateTime(),
            stopped.toKotlinLocalDateTime(),
            amount
        )
        submitter.submit(started, stopped, amount)

        setUiState(UIState.SUBMITTED)
    }

    private fun handleClear() {
        setUiState(UIState.NOT_STARTED)
        startedDateTime = null
        PersistenceManager.removeStartedDateTime(this)
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

                lastEntry?.let {
                    lastEntryText.visibility = View.VISIBLE
                    lastEntryText.text = getString(
                        R.string.last_entry_text,
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(it.started.toJavaLocalDateTime()),
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(it.stopped.toJavaLocalDateTime())
                    )
                } ?: run {
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

                startedDateTime?.let {
                    startedAtText.text = getString(
                        R.string.started_at_text,
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(it)
                    )
                }

                lastEntryText.visibility = View.GONE
            }
            UIState.SUBMITTED,
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
}