package seamonster.kraken.todo.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.repository.TaskRepo

class SignInCompletedWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        val colRef = TaskRepo.getInstance().getColRef()
        val util = AppUtil(context)
        colRef.get().addOnSuccessListener { snapshot ->
            snapshot.documents.forEach {
                val task = it.toObject(Task::class.java)
                if (task != null) {
                    task.id = it.id
                    util.scheduleTask(task)
                }
            }
        }
        return Result.success()
    }
}