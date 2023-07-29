package seamonster.kraken.todo.repository

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.util.Calendar

@ProvidedTypeConverter
class CalendarConverter {
    @TypeConverter
    fun fromTimeStamp(timeStamp: Long?): Calendar?{
        if (timeStamp == null) return null
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp
        return calendar
    }

    @TypeConverter
    fun calendarToTimeStamp(calendar: Calendar?): Long?{
        return calendar?.timeInMillis
    }
}