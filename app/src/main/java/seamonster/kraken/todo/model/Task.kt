package seamonster.kraken.todo.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.work.Data
import java.io.Serializable
import java.util.Calendar

class Task : BaseObservable(), Serializable {
    var id: String? = null

    @get:Bindable
    var title: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.title)
        }

    @get:Bindable
    var desc: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.desc)
        }

    @get:Bindable
    var important: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.important)
        }

    @get:Bindable
    var listId: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.listId)
        }

    @get:Bindable
    var repeat: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.repeat)
        }

    @get:Bindable
    var completed: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.completed)
        }

    @get:Bindable
    var year: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.year)
        }

    @get:Bindable
    var month: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.month)
        }

    @get:Bindable
    var date: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.date)
        }

    @get:Bindable
    var hour: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.hour)
        }

    @get:Bindable
    var minute: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.minute)
        }

    var createdAt: Long? = null

    fun getDateTime(): Calendar {
        return Calendar.getInstance().apply {
            set(year, month, date, hour, minute, 0)
        }
    }

    fun convertDateTime(calendar: Calendar) {
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        date = calendar.get(Calendar.DATE)
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
    }

    fun convertToData(): Data {
        return Data.Builder()
            .putString("id", id)
            .putString("title", title)
            .putString("desc", desc)
            .putString("listId", listId)
            .putBoolean("completed", completed)
            .putInt("repeat", repeat)
            .putLong("createdAt", createdAt ?: 0)
            .putInt("year", year)
            .putInt("month", month)
            .putInt("date", date)
            .putInt("hour", hour)
            .putInt("minute", minute)
            .build()
    }

    fun fromData(data: Data) {
        id = data.getString("id")
        title = data.getString("title") ?: ""
        desc = data.getString("desc") ?: ""
        listId = data.getString("listId")
        important = data.getBoolean("important", false)
        repeat = data.getInt("repeat", 0)
        completed = data.getBoolean("completed", false)
        createdAt = data.getLong("createdAt", 0)
        year = data.getInt("year", 0)
        month = data.getInt("month", 0)
        date = data.getInt("date", 0)
        hour = data.getInt("hour", 0)
        minute = data.getInt("minute", 0)
    }
}