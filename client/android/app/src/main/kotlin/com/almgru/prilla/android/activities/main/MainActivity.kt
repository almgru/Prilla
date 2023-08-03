package com.almgru.prilla.android.activities.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.almgru.prilla.android.R
import com.almgru.prilla.android.activities.login.LoginActivity
import com.almgru.prilla.android.activities.main.events.CancelSelectCustomStartDateTimeEvent
import com.almgru.prilla.android.activities.main.events.CancelSelectCustomStopDateTimeEvent
import com.almgru.prilla.android.activities.main.events.EntryAddedSuccessfullyEvent
import com.almgru.prilla.android.activities.main.events.EntryClearedEvent
import com.almgru.prilla.android.activities.main.events.EntryStartedEvent
import com.almgru.prilla.android.activities.main.events.EntrySubmitNetworkErrorEvent
import com.almgru.prilla.android.activities.main.events.EntrySubmitSessionExpiredErrorEvent
import com.almgru.prilla.android.activities.main.events.EntrySubmittedEvent
import com.almgru.prilla.android.activities.main.events.SelectCustomStartDateTimeEvent
import com.almgru.prilla.android.activities.main.events.SelectCustomStopDateTimeEvent
import com.almgru.prilla.android.databinding.ActivityMainBinding
import com.almgru.prilla.android.events.Event
import com.almgru.prilla.android.fragment.DatePickerFragment
import com.almgru.prilla.android.fragment.TimePickerFragment
import com.almgru.prilla.android.model.Entry
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.amountLabel.text = getString(R.string.amount_label, binding.amountSlider.progress)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupViewListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.events.collect(::handleEvent) }
                launch { viewModel.state.collect(::handleStateChange) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun setupViewListeners() {
        binding.startStopButton.setOnClickListener { viewModel.onStartStopPressed() }
        binding.startStopButton.setOnLongClickListener {
            viewModel.onStartStopLongPressed()
            true
        }

        binding.amountSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.updateAmount(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })

        binding.customStartLink.setOnClickListener { viewModel.onCustomStartedPressed() }
        binding.customStopLink.setOnClickListener { viewModel.onCustomStoppedPressed() }
    }

    private fun handleEvent(event: Event) = when (event) {
        is EntryStartedEvent -> setUiVisibility(UIMode.STARTED)
        is EntryClearedEvent -> {
            showMessage(R.string.entry_clear_message)
            setUiVisibility(UIMode.NOT_STARTED)
        }

        is EntrySubmittedEvent -> setUiVisibility(UIMode.SUBMITTED)
        is EntryAddedSuccessfullyEvent -> showMessage(R.string.entry_added_message)
        is EntrySubmitNetworkErrorEvent -> showMessage(R.string.entry_submit_network_error_message)
        is EntrySubmitSessionExpiredErrorEvent -> {
            showMessage(R.string.entry_submit_session_expired_message)
            returnToLoginScreen()
        }

        is SelectCustomStartDateTimeEvent -> showDateTimePicker(
            viewModel::onStartDateTimePicked, viewModel::onCancelPickStartDateTime
        )

        is SelectCustomStopDateTimeEvent -> showDateTimePicker(
            viewModel::onStopDateTimePicked, viewModel::onCancelPickStopDateTime
        )

        is CancelSelectCustomStartDateTimeEvent -> setUiVisibility(UIMode.NOT_STARTED)
        is CancelSelectCustomStopDateTimeEvent -> setUiVisibility(UIMode.STARTED)
        else -> throw IllegalArgumentException("Unknown event")
    }

    private fun handleStateChange(state: MainViewState) {
        binding.amountLabel.text = getString(R.string.amount_label, state.amount)
        handleStartedDatetimeChanged(state.startedDateTime)
        handleLastEntryChanged(state.latestEntry)
    }

    private fun handleStartedDatetimeChanged(started: LocalDateTime?) = when (started) {
        null -> binding.startedAtText.text = ""
        else -> binding.startedAtText.text = getString(
            R.string.started_at_text,
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(started)
        )
    }

    private fun handleLastEntryChanged(lastEntry: Entry?) = when (lastEntry) {
        null -> binding.lastEntryText.text = ""
        else -> binding.lastEntryText.text = getString(
            R.string.last_entry_text,
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(lastEntry.started.toJavaLocalDateTime()),
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(lastEntry.stopped.toJavaLocalDateTime())
        )
    }

    private fun returnToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private fun setUiVisibility(state: UIMode) {
        when (state) {
            UIMode.NOT_STARTED -> {
                binding.submitProgressIndicator.visibility = View.GONE
                binding.startStopButton.visibility = View.VISIBLE
                binding.startStopButton.text = getText(R.string.start_stop_button_start_text)
                binding.customStartLink.visibility = View.VISIBLE
                binding.customStopLink.visibility = View.GONE
                binding.startedAtText.visibility = View.GONE
                binding.lastEntryText.visibility = View.VISIBLE
            }

            UIMode.STARTED -> {
                binding.submitProgressIndicator.visibility = View.GONE
                binding.startStopButton.visibility = View.VISIBLE
                binding.startStopButton.text = getText(R.string.start_stop_button_stop_text)
                binding.customStartLink.visibility = View.GONE
                binding.customStopLink.visibility = View.VISIBLE
                binding.startedAtText.visibility = View.VISIBLE
                binding.lastEntryText.visibility = View.GONE
            }

            UIMode.SUBMITTED, UIMode.SELECTING_DATETIME -> {
                binding.submitProgressIndicator.visibility = View.VISIBLE
                binding.startStopButton.visibility = View.GONE
                binding.customStartLink.visibility = View.GONE
                binding.customStopLink.visibility = View.GONE
                binding.startedAtText.visibility = View.GONE
                binding.lastEntryText.visibility = View.GONE
            }
        }
    }

    private fun showDateTimePicker(callback: (LocalDateTime) -> Unit, cancelCallback: () -> Unit) {
        DatePickerFragment({ date ->
            TimePickerFragment({ time ->
                callback.invoke(LocalDateTime.of(date, time))
            }, {
                cancelCallback.invoke()
            }).show(supportFragmentManager, getString(R.string.time_picker_tag))
        }, {
            cancelCallback.invoke()
        }).show(supportFragmentManager, getString(R.string.date_picker_tag))
    }

    private fun showMessage(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }
}

private enum class UIMode {
    NOT_STARTED, STARTED, SELECTING_DATETIME, SUBMITTED
}
