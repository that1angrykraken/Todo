package seamonster.kraken.todo.util

import java.util.Calendar

class TextUtil {
    companion object {
        fun convertDateTime(
            calendar: Calendar,
            dateSeparator: String = "/",
            timeSeparator: String = ":",
            dateTimeSeparator: String = "-"
        ): String {
            val date = String.format("%02d", calendar.get(Calendar.DATE))
            val month = String.format("%02d", calendar.get(Calendar.MONTH)+1)
            val hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
            val minute = String.format("%02d", calendar.get(Calendar.MINUTE))
            val year = calendar.get(Calendar.YEAR)
            return "$date$dateSeparator$month$dateSeparator$year $dateTimeSeparator $hour$timeSeparator$minute"
        }
    }
}