package seamonster.kraken.todo.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import seamonster.kraken.todo.R
import seamonster.kraken.todo.activity.MainActivity
import seamonster.kraken.todo.model.Task
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "NotificationReceiver"
        const val CHANNEL_ID = "MAIN_CHANNEL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val task = AppUtil.getTaskFromBundle(intent.extras)
        if (task != null) {
            createNotificationChannel(context)
            showNotification(context, task)
        }
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

    private fun showNotification(context: Context, task: Task) {
        val notificationId = System.currentTimeMillis().toInt()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val bundle = Bundle().apply {
            updateDateTime(task)
            putSerializable("task", task)
            putInt("notificationId", notificationId)
        }
        val intent = Intent(context, MarkCompletedService::class.java).apply { putExtras(bundle) }
        val actionIntent =
            PendingIntent.getService(context, System.currentTimeMillis().toInt(), intent, flags)
        val intent1 = Intent(context, MainActivity::class.java).apply { putExtras(bundle) }
        val contentIntent =
            PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent1, flags)

        val actionTitle = context.getString(R.string.mark_completed)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(task.title)
            .setContentText(task.desc)
            .setContentIntent(contentIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(task.desc))
            .setPriority(if (task.important) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.round_done_48, actionTitle, actionIntent)

        NotificationManagerCompat.from(context).run {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
                val schedulerIntent = Intent(context, ScheduleTaskService::class.java).apply {
                    putExtras(bundle)
                }
                context.startForegroundService(schedulerIntent)
            }
        }
    }

    private fun updateDateTime(task: Task) {
        when (task.repeat) {
            1 -> AppUtil.updateDateTime(task, Calendar.DATE, 1)
            2 -> AppUtil.updateDateTime(task, Calendar.DATE, 7)
            3 -> AppUtil.updateDateTime(task, Calendar.MONTH, 1)
            4 -> AppUtil.updateDateTime(task, Calendar.YEAR, 1)
        }
    }
}