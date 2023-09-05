package seamonster.kraken.todo.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.FragmentEditTaskBinding
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.util.AppUtil
import seamonster.kraken.todo.viewmodel.PageViewModel
import seamonster.kraken.todo.viewmodel.TaskViewModel
import java.util.Calendar

class EditTaskFragment : DialogFragment() {

    companion object {
        const val TAG = "EditTaskFragment"
    }

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var pageViewModel: PageViewModel
    private lateinit var binding: FragmentEditTaskBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        pageViewModel = ViewModelProvider(requireActivity())[PageViewModel::class.java]
        viewModel.currentTask = pageViewModel.currentTask

        binding = FragmentEditTaskBinding.inflate(layoutInflater)
        binding.t = viewModel.currentTask

        return Dialog(requireContext(), R.style.DialogTheme).also {
            initNavigation(it)
            initButtonDelete(it)
            initButtonSave(it)
            initBottomBarButtons(it)
            initChipRepeatFrq()
            initButtonSetDateTime()
            it.window?.setWindowAnimations(R.style.DialogBottomUpAnimation)
            it.setContentView(binding.root)
        }
    }

    private fun initNavigation(dialog: Dialog) {
        binding.toolbar.setNavigationOnClickListener { dialog.dismiss() }
    }

    private fun initButtonSetDateTime() {
        if (viewModel.currentTask.year > 0) {
            binding.chipDateTime.text =
                AppUtil(requireContext()).convertDateTime(viewModel.currentTask)
        }
        binding.cardDateTime.setOnClickListener {
            showDatePicker()
        }
        binding.chipDateTime.setOnCloseIconClickListener {
            viewModel.currentTask.year = 0
        }
    }

    private fun showDatePicker() {
        val c =
            if (viewModel.currentTask.year > 0) AppUtil.getDateTimeFrom(viewModel.currentTask)
            else Calendar.getInstance()
        val dateDialog = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getText(R.string.select_date))
            .setSelection(c.timeInMillis)
            .build()
        dateDialog.addOnPositiveButtonClickListener { selected ->
            val selectedDate = Calendar.getInstance().apply {
                timeInMillis = selected
                set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, c.get(Calendar.MINUTE))
            }
            showTimePicker(selectedDate)
        }
        dateDialog.show(parentFragmentManager, null)
    }

    private fun showTimePicker(calendar: Calendar) {
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText(getText(R.string.set_time))
            .setTimeFormat(CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
            .build()
        timePicker.addOnPositiveButtonClickListener {
            calendar.let {
                it.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                it.set(Calendar.MINUTE, timePicker.minute)
            }
            viewModel.currentTask.dateTimeFrom(calendar)
            binding.chipDateTime.text =
                AppUtil(requireContext()).convertDateTime(viewModel.currentTask)
        }
        timePicker.show(parentFragmentManager, null)
    }

    private fun initChipRepeatFrq() {
        val repeat = resources.getTextArray(R.array.repeat_frequencies)
        binding.chipRepeatFrq.text = repeat[viewModel.currentTask.repeat]
        with(binding.cardRepeat) {
            setOnClickListener {
                val newValue = (viewModel.currentTask.repeat + 1) % 5
                viewModel.currentTask.repeat = newValue
                binding.chipRepeatFrq.text = repeat[newValue]
            }
        }
    }

    private fun initButtonDelete(dialog: Dialog) {
        binding.buttonDelete.setOnClickListener {
            viewModel.deleteTask()
            dialog.dismiss()
        }
    }

    private fun initButtonSave(dialog: Dialog) {
        binding.buttonSave.setOnClickListener {
            var check = pageViewModel.anyDuplicatedDateTimeTasks()
            if (check) {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(getString(R.string.similar_date_time_message))
                    .setNegativeButton(getString(R.string.dialog_negative_button)) { _, _ -> }
                    .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                        viewModel.upsertTask()
                        dialog.dismiss()
                    }.show()
            }
            else{
                viewModel.upsertTask()
                dialog.dismiss()
            }
        }
    }

    private fun initBottomBarButtons(dialog: Dialog) {
        binding.buttonMarkCompleted.setOnClickListener {
            viewModel.markCompleted(true)
            dialog.dismiss()
        }
        binding.buttonMarkUncompleted.setOnClickListener {
            viewModel.markCompleted(false)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        pageViewModel.currentTask = Task()
    }
}