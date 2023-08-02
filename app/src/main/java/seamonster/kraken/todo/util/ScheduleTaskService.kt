package seamonster.kraken.todo.util

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.repository.AppDatabase
import java.util.Calendar

class ScheduleTaskService : Service() {

    companion object {
        const val TAG = "ScheduleTaskService"
        const val NOTIFICATION_ID = 10110011
        const val CHANNEL_ID = "BACKGROUND_SERVICE"
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification())
        val bundle = intent.extras
        val task = AppUtil.getTaskFromBundle(bundle)
        if (task != null) {
            updateTask(task)
        }
        scheduleNextTask(this)
        stopSelf()
        return START_NOT_STICKY
    }

    private fun updateTask(task: Task) {
        when (task.repeat) {
            1 -> {
                val date = task.dateTime!!.get(Calendar.DATE)
                task.dateTime!!.set(Calendar.DATE, date + 1)
            }

            2 -> {
                val date = task.dateTime!!.get(Calendar.DATE)
                task.dateTime!!.set(Calendar.DATE, date + 7)
            }

            3 -> {
                val month = task.dateTime!!.get(Calendar.MONTH)
                task.dateTime!!.set(Calendar.MONTH, month + 1)
            }

            4 -> {
                val year = task.dateTime!!.get(Calendar.YEAR)
                task.dateTime!!.set(Calendar.DATE, year + 1)
            }

            else -> return
        }
        val db = AppDatabase.getInstance(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().upsert(task)
        }
    }

    private fun scheduleNextTask(context: Context) {
        Log.d(TAG, "scheduleNextTask: entry")

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val task = db.taskDao().getUpcomingTask() ?: return@launch
            val intent = Intent(context, NotificationReceiver::class.java)
            val bundle = Bundle().also { it.putSerializable("task", task) }
            intent.putExtras(bundle)
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                task.dateTime!!.timeInMillis,
                pendingIntent
            )

            Log.d(TAG, "scheduleNextTask: task scheduled")
        }
    }

    private fun notification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "BackgroundService",
            NotificationManager.IMPORTANCE_HIGH
        )
        with(NotificationManagerCompat.from(this)) {
            createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tasks")
            .setContentText("Background service is running")

        return builder.build()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}