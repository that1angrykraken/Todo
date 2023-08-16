package seamonster.kraken.todo.viewmodel

import androidx.lifecycle.ViewModel
import seamonster.kraken.todo.model.Feedback
import seamonster.kraken.todo.repository.FeedbackRepo

class FeedbackViewModel : ViewModel() {
    private val dataSource = FeedbackRepo()

    var feedback = Feedback()

    fun sendFeedback(){
        dataSource.add(feedback)
        feedback = Feedback()
    }
}