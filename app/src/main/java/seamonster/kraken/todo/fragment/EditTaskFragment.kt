package seamonster.kraken.todo.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.FragmentEditTaskBinding
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.util.AppUtil
import seamonster.kraken.todo.viewmodel.TaskViewModel
import java.util.Calendar

class EditTaskFragment : DialogFragment() {

    companion object {
        const val TAG = "EditTaskFragment"
    }

    private lateinit var viewModel: TaskViewModel
    private lateinit var binding: FragmentEditTaskBinding
    var task = Task()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        binding = FragmentEditTaskBinding.inflate(layoutInflater)
        binding.t = task

        return Dialog(requireContext(), R.style.DialogTheme).also {
            initNavigation(it)
            initButtonDelete(it)
            initButtonSave(it)
            initButtonMarkCompleted(it)
            initChipRepeatFrq()
            initButtonSetDateTime()
            binding.toolbar.setNavigationOnClickListener { _ -> it.dismiss() }
            it.window?.setWindowAnimations(R.style.DialogBottomUpAnimation)
            it.setContentView(binding.root)
        }
    }

    private fun initNavigation(dialog: Dialog) {
        binding.toolbar.setNavigationOnClickListener { dialog.dismiss() }
    }

    private fun initButtonSetDateTime() {
        if(task.year > 0){
            binding.chipDateTime.text = AppUtil().convertDateTime(requireContext(), task)
        }
        binding.cardDateTime.setOnClickListener {
            showDatePicker()
        }
        binding.chipDateTime.setOnCloseIconClickListener {
            task.year = 0
        }
    }

    private fun showDatePicker() {
        val c = if (task.year > 0) AppUtil.getDateTimeFrom(task) else Calendar.getInstance()
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
            AppUtil.parseDateTimeToTask(calendar, task)
            binding.chipDateTime.text = AppUtil().convertDateTime(requireContext(), task)
        }
        timePicker.show(parentFragmentManager, null)
    }

    private fun initChipRepeatFrq() {
        val repeat = resources.getTextArray(R.array.repeat_frequencies)
        binding.chipRepeatFrq.text = repeat[task.repeat]
        with(binding.cardRepeat) {
            setOnClickListener {
                task.repeat = (task.repeat + 1) % 5
                binding.chipRepeatFrq.text = repeat[task.repeat]
            }
        }
    }

    private fun initButtonDelete(dialog: Dialog) {
        binding.buttonDelete.setOnClickListener {
            viewModel.delete(task)
            dialog.dismiss()
        }
    }

    private fun initButtonSave(dialog: Dialog) {
        binding.buttonSave.setOnClickListener {
            if (task.title.isEmpty()) task.title = getString(R.string.default_task_title)
            viewModel.upsert(task)
            dialog.dismiss()
        }
    }

    private fun initButtonMarkCompleted(dialog: Dialog) {
        with(binding.buttonMarkCompleted) {
            text = if (task.completed) getText(R.string.mark_uncompleted)
            else getText(R.string.mark_completed)
            setOnClickListener {
                task.completed = !task.completed
                viewModel.upsert(task)
                if (task.completed) {
                    text = getText(R.string.mark_uncompleted)
                    dialog.dismiss()
                } else text = getText(R.string.mark_completed)
            }
        }
    }
}