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
import com.almgru.prilla.android.databinding.ActivityMainBinding
import com.almgru.prilla.android.fragment.DatePickerFragment
import com.almgru.prilla.android.fragment.TimePickerFragment
import com.almgru.prilla.android.model.Entry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.launch

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

    private fun handleEvent(event: EntryEvent) = when (event) {
        is EntryEvent.Started -> setUiVisibility(UIMode.STARTED)
        is EntryEvent.Cleared -> {
            showMessage(R.string.entry_clear_message)
            setUiVisibility(UIMode.NOT_STARTED)
        }

        is EntryEvent.Submitted -> setUiVisibility(UIMode.SUBMITTED)
        is EntryEvent.Stored -> showMessage(R.string.entry_added_message)
        is EntryEvent.NetworkError -> showMessage(R.string.network_error_message)
        is EntryEvent.InvalidCredentialsError -> {
            showMessage(R.string.session_expired_error_message)
            returnToLoginScreen()
        }

        is EntryEvent.PickStartedDatetimeRequest -> showDateTimePicker(
            viewModel::onStartDateTimePicked,
            viewModel::onCancelPickStartDateTime
        )

        is EntryEvent.PickStoppedDatetimeRequest -> showDateTimePicker(
            viewModel::onStopDateTimePicked,
            viewModel::onCancelPickStopDateTime
        )

        is EntryEvent.CancelledPickStartedDatetime -> setUiVisibility(UIMode.NOT_STARTED)
        is EntryEvent.CancelledPickStoppedDatetime -> setUiVisibility(UIMode.STARTED)
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
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(lastEntry.started),
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(lastEntry.stopped)
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
