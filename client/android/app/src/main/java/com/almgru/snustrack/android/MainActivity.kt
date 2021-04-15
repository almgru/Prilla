package com.almgru.snustrack.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.almgru.snustrack.android.net.CookieStorage
import com.almgru.snustrack.android.net.EntryAddedListener
import com.almgru.snustrack.android.net.EntrySubmitter
import com.android.volley.*
import java.time.LocalDateTime

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener,
    EntryAddedListener, View.OnLongClickListener {
    private enum class UIState {
        NOT_STARTED, STARTED, SUBMITTED,
    }

    private lateinit var submitter: EntrySubmitter

    private var startedDateTime: LocalDateTime? = null

    private lateinit var startStopButton: Button
    private lateinit var amountSlider: SeekBar
    private lateinit var amountLabel: TextView
    private lateinit var submitProgressIndicator : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startStopButton = findViewById(R.id.startStopButton)
        amountSlider = findViewById(R.id.amountSlider)
        amountLabel = findViewById(R.id.amountLabel)
        submitProgressIndicator = findViewById(R.id.submitProgressIndicator)

        startStopButton.setOnLongClickListener(this)
        amountLabel.text = getString(R.string.amount_label).format(amountSlider.progress)
        amountSlider.setOnSeekBarChangeListener(this)

        submitter = EntrySubmitter(this, this)
    }

    override fun onResume() {
        super.onResume()

        startedDateTime = PersistenceManager.getStartedDateTime(this)

        if (startedDateTime != null) {
            setUiState(UIState.STARTED)
        } else {
            setUiState(UIState.NOT_STARTED)
        }
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
            }
            UIState.STARTED -> {
                submitProgressIndicator.visibility = View.GONE
                startStopButton.visibility = View.VISIBLE
                startStopButton.text = getText(R.string.start_stop_button_stop_text)
            }
            UIState.SUBMITTED -> {
                submitProgressIndicator.visibility = View.VISIBLE
                startStopButton.visibility = View.GONE
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
}