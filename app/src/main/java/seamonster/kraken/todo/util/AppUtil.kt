package seamonster.kraken.todo.util

import android.content.Context
import android.os.Build
import android.os.Bundle
import seamonster.kraken.todo.R
import seamonster.kraken.todo.model.Task
import java.util.Calendar

class AppUtil {

    fun convertDateTime(context: Context, t: Task): String {
        val c1 = Calendar.getInstance()
        val c2 = getDateTimeFrom(t)
        val datePart = if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
            c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
        ) {
            when (c1.get(Calendar.DATE) - c2.get(Calendar.DATE)) {
                -2 -> context.getString(R.string.date_day_after_tomorrow)
                -1 -> context.getString(R.string.date_tomorrow)
                0 -> context.getString(R.string.date_today)
                1 -> context.getString(R.string.date_yesterday)
                else -> convertDate(c2)
            }
        } else convertDate(c2)
        val hour = String.format("%02d", t.hour)
        val minute = String.format("%02d", t.minute)
        return "$datePart - $hour:$minute"
    }

    companion object {

        fun updateDateTime(t: Task, field: Int, i: Int){
            val c = Calendar.getInstance().apply {
                set(t.year, t.month, t.date, t.hour, t.minute)
                set(field, get(field) + i)
            }
            t.year = c.get(Calendar.YEAR)
            t.month = c.get(Calendar.MONTH)
            t.date = c.get(Calendar.DATE)
            t.hour = c.get(Calendar.HOUR_OF_DAY)
            t.minute = c.get(Calendar.MINUTE)
        }

        fun parseDateTimeToTask(c: Calendar, t: Task){
            t.year = c.get(Calendar.YEAR)
            t.month = c.get(Calendar.MONTH)
            t.date = c.get(Calendar.DATE)
            t.hour = c.get(Calendar.HOUR_OF_DAY)
            t.minute = c.get(Calendar.MINUTE)
        }

        fun getDateTimeFrom(t: Task): Calendar {
            return Calendar.getInstance()
                .apply { set(t.year, t.month, t.date, t.hour, t.minute, 0) }
        }

        fun afterNow(t: Task?): Boolean{
            if (t == null) return false
            return getDateTimeFrom(t).after(Calendar.getInstance())
        }

        private fun convertDate(calendar: Calendar): String {
            val date = String.format("%02d", calendar.get(Calendar.DATE))
            val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
            val year = calendar.get(Calendar.YEAR)
            return "$date/$month/$year"
        }

        fun getTaskFromBundle(bundle: Bundle?): Task? {
            if (bundle == null) return null
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                bundle.getSerializable("task", Task::class.java)
            else @Suppress("DEPRECATION") bundle.getSerializable("task") as Task
        }
    }
}