package seamonster.kraken.todo.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Calendar

@Entity("tasks")
class Task(@PrimaryKey(autoGenerate = true) val id: Int = 0) : BaseObservable(), Serializable {

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
    var listId: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.listId)
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
    var hour: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.hour)
        }

    @get:Bindable
    var minute: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.minute)
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

    @get:Ignore
    var dateTime: Calendar
        get() {
            val c = Calendar.getInstance()
            c.set(year, month, date, hour ?: 0, minute ?: 0, 0)
            return c
        }
        set(value) {
            year = value.get(Calendar.YEAR)
            month = value.get(Calendar.MONTH)
            date = value.get(Calendar.DATE)
            hour = value.get(Calendar.HOUR_OF_DAY)
            minute = value.get(Calendar.MINUTE)
        }

    @get:Ignore
    val uncompleted: Boolean
        get() = Calendar.getInstance().after(dateTime)
}