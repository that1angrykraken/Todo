package seamonster.kraken.todo.util

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import seamonster.kraken.todo.R
import seamonster.kraken.todo.activity.MainActivity
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.repository.TaskRepo

class NotificationWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    companion object {
        const val TAG = "NotificationWorker"
        const val CHANNEL_ID = "MAIN_CHANNEL"
    }

    private val repo = TaskRepo.getInstance()

    override fun doWork(): Result {
        val task = Task().apply { fromData(inputData) }
        showNotification(task)
        return Result.success()
    }

    private fun showNotification(task: Task) {
        val bundle = Bundle().apply { putSerializable("task", task) }
        val notification = Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.round_task_alt_48)
            .setContentTitle(task.title)
            .setContentText(task.desc)
            .setContentIntent(contentIntent(bundle))
            .setStyle(BigTextStyle().bigText(task.desc))
            .setPriority(if (task.important) PRIORITY_MAX else PRIORITY_HIGH)
            .setWhen(task.dateTime().timeInMillis)
            .setSubText(if (task.important) context.getString(R.string.important) else null)
            .addAction(actionMarkCompleted(bundle))
            .setAutoCancel(true)
            .build()
        val id = (task.createdAt ?: System.currentTimeMillis()).toInt()
        notify(id, notification)
        updateTask(task)
    }

    private fun notify(id: Int, notification: Notification){
        with(NotificationManagerCompat.from(context)) {
            if (getNotificationChannel(CHANNEL_ID) == null) { // create a notification channel
                val channel = NotificationChannelCompat
                    .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName(context.getString(R.string.main_channel_name))
                    .build()
                createNotificationChannel(channel)
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                notify(id, notification)
                Log.d(TAG, "notify: ok")
            }
        }
    }

    private fun contentIntent(bundle: Bundle): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply { putExtras(bundle) }
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(context, 1, intent, flags)
    }

    private fun actionMarkCompleted(bundle: Bundle): Action {
        val intent = Intent(context, MarkCompletedService::class.java).apply { putExtras(bundle) }
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getService(context, 1, intent, flags)
        return Action.Builder(
            R.drawable.round_done_48,
            context.getString(R.string.mark_completed),
            pendingIntent
        ).build()
    }

    private fun updateTask(task: Task) {
        AppUtil.updateDateTime(task)
        if (task.repeat != 0) {
            AppUtil(context).scheduleTask(task)
        }
        repo.upsert(task)
    }
}