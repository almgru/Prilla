package se.algr.prilla.android.view.main.implementation

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.launch
import se.algr.prilla.android.R
import se.algr.prilla.android.databinding.ActivityMainBinding
import se.algr.prilla.android.model.CompleteEntry
import se.algr.prilla.android.view.ApiError
import se.algr.prilla.android.view.fragment.CancelableDialogFragment
import se.algr.prilla.android.view.fragment.DatePickerFragment
import se.algr.prilla.android.view.fragment.TimePickerFragment
import se.algr.prilla.android.view.login.implementation.LoginActivity
import se.algr.prilla.android.view.main.events.MainViewEvent
import se.algr.prilla.android.view.main.state.MainViewState

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var openDirectoryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.amountLabel.text = getString(R.string.amount_label, binding.amountSlider.progress)

        setupViewListeners()

        binding.banner.setLeftButtonAction { binding.banner.dismiss() }
        binding.banner.setRightButtonAction {
            binding.banner.dismiss()

            CancelableDialogFragment(
                R.string.backup_setup_title,
                R.string.backup_setup_message,
                R.string.backup_setup_confirm,
                R.string.backup_setup_cancel
            ) {
                requestStoragePermission()
            }.show(supportFragmentManager, getString(R.string.backup_setup_dialog_tag))
        }

        openDirectoryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    result.data?.data?.also { uri ->
                        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                        contentResolver.takePersistableUriPermission(uri, flags)
                        viewModel.onStoragePermissionGranted(uri)
                    }
                }
                RESULT_CANCELED -> TODO()
                else -> error("Unexpected result code")
            }
        }

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

        binding.amountSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.updateAmount(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })

        binding.customStartLink.setOnClickListener { viewModel.onCustomStartedPressed() }
        binding.customStopLink.setOnClickListener { viewModel.onCustomStoppedPressed() }
    }

    private fun handleEvent(event: MainViewEvent) = when (event) {
        MainViewEvent.EntryStarted -> setUiVisibility(UIMode.STARTED)
        MainViewEvent.EntryCleared -> {
            showMessage(R.string.entry_clear_message)
            setUiVisibility(UIMode.NOT_STARTED)
        }

        MainViewEvent.EntrySubmitted -> setUiVisibility(UIMode.SUBMITTED)
        MainViewEvent.InvalidCredentialsError -> returnToLoginScreen(ApiError.SessionExpiredError)
        MainViewEvent.SslHandshakeError -> returnToLoginScreen(ApiError.SslHandshakeError)
        MainViewEvent.NetworkError -> returnToLoginScreen(ApiError.NetworkError)

        MainViewEvent.BackupSuccessful -> showMessage(R.string.backup_complete_message)
        MainViewEvent.BackupRequiresPermission -> binding.banner.show()
        MainViewEvent.BackupUnsupported -> showMessage(R.string.backup_unsupported_message)
        MainViewEvent.BackupIoError -> showMessage(R.string.backup_io_error)

        MainViewEvent.EntryStored -> {
            showMessage(R.string.entry_added_message)
            setUiVisibility(UIMode.NOT_STARTED)
        }

        MainViewEvent.PickStartedDatetimeRequest -> showDateTimePicker(
            viewModel::onStartDateTimePicked,
            viewModel::onCancelPickStartDateTime
        )

        MainViewEvent.PickStoppedDatetimeRequest -> showDateTimePicker(
            viewModel::onStopDateTimePicked,
            viewModel::onCancelPickStopDateTime
        )

        MainViewEvent.CancelledPickStartedDatetime -> setUiVisibility(UIMode.NOT_STARTED)
        MainViewEvent.CancelledPickStoppedDatetime -> setUiVisibility(UIMode.STARTED)
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

    private fun handleLastEntryChanged(lastEntry: CompleteEntry?) = when (lastEntry) {
        null -> binding.lastEntryText.text = ""
        else -> binding.lastEntryText.text = getString(
            R.string.last_entry_text,
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(lastEntry.started),
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(lastEntry.stopped)
        )
    }

    private fun returnToLoginScreen(apiError: ApiError) {
        setUiVisibility(UIMode.STARTED)
        startActivity(
            Intent().apply {
                component = ComponentName(this@MainActivity, LoginActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                putExtra("error", apiError)
            }
        )
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

    private fun showMessage(resId: Int, length: Int = Snackbar.LENGTH_SHORT) = Snackbar.make(
        binding.coordinatorLayout,
        resId,
        length
    )
        .show()

    private fun requestStoragePermission() = openDirectoryLauncher.launch(
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, "/")
            }
        }
    )
}

private enum class UIMode {
    NOT_STARTED, STARTED, SELECTING_DATETIME, SUBMITTED
}
