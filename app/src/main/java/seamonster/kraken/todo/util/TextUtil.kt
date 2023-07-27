package seamonster.kraken.todo.util

import android.content.Context
import seamonster.kraken.todo.R
import java.util.Calendar

class TextUtil {
    companion object {

        fun convertDateTime(context: Context, calendar: Calendar): String {
            val c = Calendar.getInstance()
            val datePart = if (c.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                c.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            ) {
                when (c.get(Calendar.DATE) - calendar.get(Calendar.DATE)) {
                    -2 -> context.getString(R.string.date_day_after_tomorrow)
                    -1 -> context.getString(R.string.date_tomorrow)
                    0 -> context.getString(R.string.date_today)
                    1 -> context.getString(R.string.date_yesterday)
                    else -> convertDate(calendar)
                }
            } else convertDate(calendar)
            val hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
            val minute = String.format("%02d", calendar.get(Calendar.MINUTE))
            return "$datePart - $hour:$minute"
        }

        private fun convertDate(calendar: Calendar): String {
            val date = String.format("%02d", calendar.get(Calendar.DATE))
            val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
            val year = calendar.get(Calendar.YEAR)
            return "$date/$month/$year"
        }
    }
}