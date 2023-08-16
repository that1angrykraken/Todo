package seamonster.kraken.todo.repository

import com.google.firebase.firestore.FirebaseFirestore

abstract class DataSource {
    companion object {
        fun taskReference(userEmail: String?) =
            FirebaseFirestore.getInstance().collection("users/$userEmail/tasks")

        fun listReference(userEmail: String?) =
            FirebaseFirestore.getInstance().collection("users/$userEmail/lists")

        fun feedbackReference() = FirebaseFirestore.getInstance().collection("feedbacks")
    }
}