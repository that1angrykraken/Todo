package seamonster.kraken.todo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import seamonster.kraken.todo.model.User

class UserViewModel : ViewModel() {
    companion object {
        const val TAG = "UserViewModel"
    }

    val currentUser = MutableLiveData(Firebase.auth.currentUser)

    val currentUserInfo: LiveData<User> = currentUser.switchMap {
        MutableLiveData(User().apply {
            id = it?.uid
            displayName = it?.displayName
            email = it?.email
            profilePhoto = it?.photoUrl
        })
    }
}