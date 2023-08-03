package seamonster.kraken.todo.util

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import seamonster.kraken.todo.persistence.AppDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ScheduleNextTaskWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val TAG = "ScheduleTaskService"
    }

    override fun doWork(): Result {
        val taskId = inputData.getInt("taskId", 0)
        if (taskId > 0) {
            updateTask(taskId)
        }
        scheduleNextTask(applicationContext)
        return Result.success()
    }

    private fun scheduleNextTask(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val task = db.taskDao().getUpcomingTask(Calendar.getInstance()) ?: return@launch
            Log.d(TAG, "scheduleNextTask: ${task.id}, ${task.desc}")

            val delayInMillis = task.dateTime!!.timeInMillis - System.currentTimeMillis()
            val data = Data.Builder()
                .putInt("taskId", task.id)
                .build()
            val notifier = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInputData(data)
                .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                .build()
            val uniqueWorkName = "scheduled_task"

            WorkManager.getInstance(context)
                .enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, notifier)

            Log.d(TAG, "scheduleNextTask: task scheduled")
        }
    }

    private fun updateTask(taskId: Int) {
        val db = AppDatabase.getInstance(this.applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            var task = db.taskDao().getTaskById(taskId)
            task = AppUtil.updateTaskDateTime(task)
            db.taskDao().upsert(task)
        }
    }

}