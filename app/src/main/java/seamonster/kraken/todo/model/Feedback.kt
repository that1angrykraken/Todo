package seamonster.kraken.todo.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class Feedback : BaseObservable() {
    var id: String? = null

    @get:Bindable
    var fTitle: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.fTitle)
        }

    @get:Bindable
    var detail: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.detail)
        }
}