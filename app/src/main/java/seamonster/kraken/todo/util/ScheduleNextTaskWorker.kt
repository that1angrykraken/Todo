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
import seamonster.kraken.todo.model.Task
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
            val task = db.taskDao().getTaskById(taskId)
            updateTaskDateTime(task)
            db.taskDao().upsert(task)
        }
    }

    private fun updateTaskDateTime(task: Task) {
        when (task.repeat) {
            1 -> {
                task.dateTime?.apply { set(Calendar.DATE, get(Calendar.DATE) + 1) }
            }

            2 -> {
                task.dateTime?.apply { set(Calendar.DATE, get(Calendar.DATE) + 7) }
            }

            3 -> {
                task.dateTime?.apply { set(Calendar.MONTH, get(Calendar.MONTH) + 1) }
            }

            4 -> {
                task.dateTime?.apply { set(Calendar.YEAR, get(Calendar.YEAR) + 1) }
            }
        }
    }

}