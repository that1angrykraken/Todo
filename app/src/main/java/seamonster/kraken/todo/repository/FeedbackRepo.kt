package seamonster.kraken.todo.repository

import android.util.Log
import seamonster.kraken.todo.model.Feedback

class FeedbackRepo {
    companion object{
        const val TAG = "FeedbackRepo"
    }

    private val collectionReference = DataSource.feedbackReference()

    fun add(feedback: Feedback){
        collectionReference.add(feedback)
            .addOnSuccessListener {
                Log.d(TAG, "add: OK")
            }
            .addOnFailureListener {
                Log.e(TAG, "add: FAILED", it)
            }
    }
}