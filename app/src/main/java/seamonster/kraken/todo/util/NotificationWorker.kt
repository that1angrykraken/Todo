package seamonster.kraken.todo.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import seamonster.kraken.todo.R
import seamonster.kraken.todo.activity.MainActivity
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.persistence.AppDatabase

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val TAG = "NotificationWorker"
        const val CHANNEL_ID = "MAIN_CHANNEL"
    }

    override fun doWork(): Result {
        val taskId = inputData.getInt("taskId", 0)
        if (taskId > 0){
            createNotificationChannel(applicationContext)
            notifyTask(applicationContext, taskId)
        }
        return Result.success()
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        with(NotificationManagerCompat.from(context)) {
            if (getNotificationChannel(CHANNEL_ID) == null) {
                createNotificationChannel(channel)
            }
        }
    }

    private fun notifyTask(context: Context, taskId: Int){
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val task = db.taskDao().getTaskById(taskId)
            showNotification(context, task)
        }
    }

    private fun showNotification(context: Context, task: Task?) {
        if (task == null) return

        val notificationId = task.id
        val actionTitle = context.getString(R.string.mark_completed)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val bundle = Bundle().apply { putSerializable("task", task) }
        val intent = Intent(context, MarkCompletedService::class.java).apply { putExtras(bundle) }
        val actionIntent =
            PendingIntent.getService(context, task.id, intent, flags)
        val intent1 = Intent(context, MainActivity::class.java).apply { putExtras(bundle) }
        val contentIntent =
            PendingIntent.getActivity(context, task.id, intent1, flags)

        val builder = Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(task.title)
            .setContentText(task.desc)
            .setContentIntent(contentIntent)
            .setStyle(BigTextStyle().bigText(task.desc))
            .setPriority(if (task.important) PRIORITY_MAX else PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.round_done_48, actionTitle, actionIntent)

        NotificationManagerCompat.from(context).run {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
                AppUtil().scheduleNextTask(context, task)
            }
        }
    }

}