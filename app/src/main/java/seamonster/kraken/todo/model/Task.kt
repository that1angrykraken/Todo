package seamonster.kraken.todo.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import com.google.firebase.Timestamp
import java.io.Serializable

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
}