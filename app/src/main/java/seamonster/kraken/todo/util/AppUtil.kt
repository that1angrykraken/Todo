package seamonster.kraken.todo.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import seamonster.kraken.todo.R
import seamonster.kraken.todo.model.Task
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AppUtil(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    @Suppress("DEPRECATION")
    fun startBackgroundService(){
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
        val className = ScheduleTaskService::class.java.name
        val check = runningServices.any { it.service.className == className }
        if (!check) {
            Log.d(TAG, "startBackgroundService: $className")
            context.startForegroundService(Intent(context, ScheduleTaskService::class.java))
        }
    }

    fun scheduleTask(task: Task) {
        if (!afterNow(task)) return
        val data = task.convertToData()
        val delay = task.dateTime().timeInMillis - System.currentTimeMillis()
        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints())
            .setInputData(data)
            .addTag("task")
            .build()
        workManager.enqueueUniqueWork(task.id!!, ExistingWorkPolicy.REPLACE, request)
        Log.d(TAG, "scheduleTask: scheduled - ${task.id}")
    }

    fun scheduleTasks(){
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        val request = OneTimeWorkRequestBuilder<SignInCompletedWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueue(request)
    }

    fun cancelTask(task: Task){
        workManager.cancelUniqueWork(task.id!!)
        Log.d(TAG, "cancelTask: canceled - ${task.id}")
    }

    fun cancelAllWork(){
        workManager.cancelAllWork()
    }

    fun startReminder() {
        val timeAt = Calendar.getInstance().apply {
            if (get(Calendar.HOUR_OF_DAY) >= 21) {
                set(Calendar.DATE, get(Calendar.DATE) + 1)
            }
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val timeDiff = timeAt - System.currentTimeMillis()
        val request = PeriodicWorkRequestBuilder<ReminderTasksWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setConstraints(constraints())
            .addTag("daily reminder")
            .build()

        workManager
            .enqueueUniquePeriodicWork(
                "reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    private fun constraints() : Constraints{
        return Constraints.Builder()
            .setRequiresDeviceIdle(false)
            .setRequiresBatteryNotLow(false)
            .build()
    }

    fun convertDateTime(t: Task): String {
        val c1 = Calendar.getInstance()
        val c2 = getDateTimeFrom(t)
        val datePart = if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
            c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
        ) {
            when (c1.get(Calendar.DATE) - c2.get(Calendar.DATE)) {
                -2 -> context.getString(R.string.date_day_after_tomorrow)
                -1 -> context.getString(R.string.date_tomorrow)
                0 -> context.getString(R.string.date_today)
                1 -> context.getString(R.string.date_yesterday)
                else -> convertDate(c2)
            }
        } else convertDate(c2)
        val hour = String.format("%02d", t.hour)
        val minute = String.format("%02d", t.minute)
        return "$datePart - $hour:$minute"
    }

    companion object {
        const val TAG = "AppUtil"

        private fun updateDateTime(t: Task, field: Int, i: Int) {
            val c = Calendar.getInstance().apply {
                set(t.year, t.month, t.date, t.hour, t.minute)
                set(field, get(field) + i)
            }
            t.year = c.get(Calendar.YEAR)
            t.month = c.get(Calendar.MONTH)
            t.date = c.get(Calendar.DATE)
            t.hour = c.get(Calendar.HOUR_OF_DAY)
            t.minute = c.get(Calendar.MINUTE)
        }

        fun getDateTimeFrom(t: Task): Calendar {
            return Calendar.getInstance()
                .apply { set(t.year, t.month, t.date, t.hour, t.minute, 0) }
        }

        fun afterNow(t: Task?): Boolean {
            if (t == null) return false
            return getDateTimeFrom(t).after(Calendar.getInstance())
        }

        private fun convertDate(calendar: Calendar): String {
            val date = String.format("%02d", calendar.get(Calendar.DATE))
            val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
            val year = calendar.get(Calendar.YEAR)
            return "$date/$month/$year"
        }

        fun getTaskFromBundle(bundle: Bundle?): Task? {
            if (bundle == null) return null
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                bundle.getSerializable("task", Task::class.java)
            else
                @Suppress("DEPRECATION") bundle.getSerializable("task") as Task
        }

        fun updateDateTime(task: Task) {
            when (task.repeat) {
                1 -> updateDateTime(task, Calendar.DATE, 1)
                2 -> updateDateTime(task, Calendar.DATE, 7)
                3 -> updateDateTime(task, Calendar.MONTH, 1)
                4 -> updateDateTime(task, Calendar.YEAR, 1)
            }
        }
    }
}