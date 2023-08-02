package com.almgru.prilla.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.almgru.prilla.android.MainViewModel.MainViewEvents.CANCEL_SELECT_CUSTOM_START_DATETIME
import com.almgru.prilla.android.MainViewModel.MainViewEvents.CANCEL_SELECT_CUSTOM_STOP_DATETIME
import com.almgru.prilla.android.MainViewModel.MainViewEvents.ENTRY_CLEARED
import com.almgru.prilla.android.MainViewModel.MainViewEvents.ENTRY_STARTED
import com.almgru.prilla.android.MainViewModel.MainViewEvents.ENTRY_SUBMITTED
import com.almgru.prilla.android.MainViewModel.MainViewEvents.ENTRY_SUBMIT_ERROR
import com.almgru.prilla.android.MainViewModel.MainViewEvents.ENTRY_SUBMIT_SUCCESS
import com.almgru.prilla.android.MainViewModel.MainViewEvents.SELECT_CUSTOM_START_DATETIME
import com.almgru.prilla.android.MainViewModel.MainViewEvents.SELECT_CUSTOM_STOP_DATETIME
import com.almgru.prilla.android.databinding.ActivityMainBinding
import com.almgru.prilla.android.fragment.DatePickerFragment
import com.almgru.prilla.android.fragment.TimePickerFragment
import com.almgru.prilla.android.model.Entry
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {
    private enum class UIState {
        NOT_STARTED, STARTED, SELECTING_DATETIME, SUBMITTED
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.amountLabel.text = getString(R.string.amount_label, binding.amountSlider.progress)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupViewListeners()
        setupStateChangeHandlers()
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

    private fun setupStateChangeHandlers() {
        lifecycleScope.launch {
            viewModel
                .state
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { state ->
                    state.event?.getContentIfNotHandled()?.let { content -> handleEvent(content) }
                    binding.amountLabel.text = getString(R.string.amount_label, state.amount)
                    handleStartedDatetimeChanged(state.startedDateTime)
                    handleLastEntryChanged(state.lastEntry)
                }
        }
    }

    private fun handleEvent(event: MainViewModel.MainViewEvents) = when (event) {
        ENTRY_STARTED -> setUiVisibility(UIState.STARTED)
        ENTRY_CLEARED -> {
            showMessage(R.string.entry_clear_message)
            setUiVisibility(UIState.NOT_STARTED)
        }

        ENTRY_SUBMITTED -> Unit
        ENTRY_SUBMIT_SUCCESS -> showMessage(R.string.entry_added_message)
        ENTRY_SUBMIT_ERROR -> {
            showMessage(R.string.entry_submit_failed_message)
            returnToLoginScreen()
        }

        SELECT_CUSTOM_START_DATETIME -> showDateTimePicker(
            viewModel::onStartDateTimePicked,
            viewModel::onCancelPickStartDateTime
        )

        SELECT_CUSTOM_STOP_DATETIME -> showDateTimePicker(
            viewModel::onStopDateTimePicked,
            viewModel::onCancelPickStopDateTime
        )

        CANCEL_SELECT_CUSTOM_START_DATETIME -> setUiVisibility(UIState.NOT_STARTED)
        CANCEL_SELECT_CUSTOM_STOP_DATETIME -> setUiVisibility(UIState.STARTED)
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
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .format(lastEntry.started.toJavaLocalDateTime()),
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .format(lastEntry.stopped.toJavaLocalDateTime())
        )
    }

    private fun returnToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private fun setUiVisibility(state: UIState) {
        when (state) {
            UIState.NOT_STARTED -> {
                binding.submitProgressIndicator.visibility = View.GONE
                binding.startStopButton.visibility = View.VISIBLE
                binding.startStopButton.text = getText(R.string.start_stop_button_start_text)
                binding.customStartLink.visibility = View.VISIBLE
                binding.customStopLink.visibility = View.GONE
                binding.startedAtText.visibility = View.GONE
                binding.lastEntryText.visibility = View.VISIBLE
            }

            UIState.STARTED -> {
                binding.submitProgressIndicator.visibility = View.GONE
                binding.startStopButton.visibility = View.VISIBLE
                binding.startStopButton.text = getText(R.string.start_stop_button_stop_text)
                binding.customStartLink.visibility = View.GONE
                binding.customStopLink.visibility = View.VISIBLE
                binding.startedAtText.visibility = View.VISIBLE
                binding.lastEntryText.visibility = View.GONE
            }

            UIState.SUBMITTED,
            UIState.SELECTING_DATETIME -> {
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