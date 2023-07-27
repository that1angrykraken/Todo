package seamonster.kraken.todo.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.ActivityEditTaskBinding
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.util.TextUtil
import java.util.Calendar

class EditTaskActivity : AppCompatActivity() {

    companion object {
        const val TAG: String = "EditTaskActivity"
    }

    private lateinit var binding: ActivityEditTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initT()
        initNavigationButton()
        initCheckboxImportant()
        initButtonDateTime()
        initButtonRepeatFrq()
        initButtonDelete()
        initSaveButton()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun initButtonRepeatFrq() {
        binding.run {
            val repeat = resources.getStringArray(R.array.repeat_frequencies)
            chipRepeatFrq.text = repeat[t!!.repeat]
            chipRepeatFrq.setOnClickListener {
                t?.repeat = (t!!.repeat + 1) % 5
                chipRepeatFrq.text = repeat[t!!.repeat]
            }
            cardRepeat.setOnClickListener {
                t?.repeat = (t!!.repeat + 1) % 5
                chipRepeatFrq.text = repeat[t!!.repeat]
            }
        }
    }

    private fun initButtonDateTime() {
        binding.cardDateTime.setOnClickListener {
            showDatePicker()
        }
        binding.chipDateTime.text = TextUtil.convertDateTime(this, binding.t!!.dateTime)
        binding.chipDateTime.setOnClickListener {
            showDatePicker()
        }
        binding.chipDateTime.setOnCloseIconClickListener {
            binding.t!!.year = 0
        }
    }

    private fun showDatePicker() {
        val calendar = if (binding.t!!.year > 0) binding.t!!.dateTime else Calendar.getInstance()
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(calendar.timeInMillis)
            .build()
        datePicker.addOnPositiveButtonClickListener {
            calendar.timeInMillis = it
            showTimePicker(calendar)
        }
        datePicker.show(supportFragmentManager, "DatePicker")
    }

    private fun showTimePicker(calendar: Calendar) {
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText(getString(R.string.set_time))
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(INPUT_MODE_KEYBOARD)
            .setHour(binding.t?.hour ?: calendar.get(Calendar.HOUR))
            .setMinute(binding.t?.minute ?: calendar.get(Calendar.MINUTE))
            .build()
        timePicker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            binding.t!!.dateTime = calendar
            binding.chipDateTime.text = TextUtil.convertDateTime(this, calendar)
        }
        timePicker.show(supportFragmentManager, "TimePicker")
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun initT() {
        val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("t", Task::class.java)!!
        else
            intent.getSerializableExtra("t") as Task
        binding.t = task
    }

    private fun setResultData(a: Int) {
        val action: Int = a
        val data = Intent()
        if (binding.t!!.title.isEmpty()) binding.t!!.title = getString(R.string.default_task_title)
        data.putExtra("t", binding.t)
        data.putExtra("a", action)
        setResult(RESULT_OK, data)
        finish()
        Log.d(TAG, "setResultData: action = $action")
    }

    private fun initSaveButton() {
        binding.buttonSave.setOnClickListener {
            setResultData(if (binding.t!!.id > 0) 2 else 0)
        }
    }

    private fun initNavigationButton() {
        binding.toolbar.setNavigationOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun initCheckboxImportant() {
        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)

        binding.checkboxImportant.addOnCheckedStateChangedListener { checkBox, state ->
            if (state == MaterialCheckBox.STATE_CHECKED) {
                checkBox.buttonTintList = getColorStateList(R.color.yellow_star)
            } else {
                checkBox.buttonTintList = getColorStateList(typedValue.resourceId)
            }
        }
    }

    private fun initButtonDelete() {
        if (binding.t!!.id > 0) {
            binding.buttonDelete.setOnClickListener { setResultData(3) }
        }
    }
}