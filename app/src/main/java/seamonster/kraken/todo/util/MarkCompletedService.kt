package seamonster.kraken.todo.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.persistence.AppDatabase

class MarkCompletedService : Service() {

    companion object {
        const val TAG = "MarkCompletedService"
        const val NOTIFICATION_ID = 10001111
        const val CHANNEL_ID = "BACKGROUND_SERVICE"
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification())
        val task = AppUtil.getTaskFromBundle(intent.extras)
        if (task != null) {
            updateTask(task.also { it.completed = true })
            cancelNotification(task.id)
        }
        stopSelf()
        return START_NOT_STICKY
    }

    private fun cancelNotification(notificationId: Int) {
        with(NotificationManagerCompat.from(this)) {
            cancel(notificationId)
        }
    }

    private fun updateTask(task: Task) {
        val db = AppDatabase.getInstance(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().upsert(task)
        }
    }

    private fun notification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "BackgroundService",
            NotificationManager.IMPORTANCE_DEFAULT
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