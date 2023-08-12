package seamonster.kraken.todo.model

import android.net.Uri
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class User : BaseObservable() {
    @get:Bindable
    var id: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.id)
        }

    @get:Bindable
    var email: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.email)
        }

    @get:Bindable
    var displayName: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.displayName)
        }

    @get:Bindable
    var profilePhoto: Uri? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.profilePhoto)
        }

}