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
    var dateTime: Calendar? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.dateTime)
        }

    @get:Ignore
    val uncompleted: Boolean
        get() = Calendar.getInstance().after(dateTime) && !completed
}