package seamonster.kraken.todo.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.repository.TaskRepo

class MarkCompletedService : Service() {

    companion object {
        const val TAG = "MarkCompletedService"
        const val NOTIFICATION_ID = 10001111
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification())
        val task = AppUtil.getTaskFromBundle(intent.extras)
        val notificationId = intent.extras?.getInt("notificationId",0)
        if (task != null && notificationId != null) {
            updateTask(task.also { it.completed = true })
            cancelNotification(notificationId)
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
        TaskRepo().upsertTask(task)
    }

    private fun notification(): Notification {
        with(NotificationManagerCompat.from(this)) {
            val channel = NotificationChannel(
                ScheduleTaskService.CHANNEL_ID,
                "BackgroundService",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            if (getNotificationChannel(ScheduleTaskService.CHANNEL_ID) == null){
                createNotificationChannel(channel)
            }
        }

        val builder = NotificationCompat.Builder(this, ScheduleTaskService.CHANNEL_ID)
            .setContentTitle("Background service")

        return builder.build()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}