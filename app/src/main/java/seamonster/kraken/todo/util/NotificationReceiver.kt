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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationManagerCompat
import seamonster.kraken.todo.R
import seamonster.kraken.todo.model.Task

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "NotificationReceiver"
        const val CHANNEL_ID = "MAIN_CHANNEL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        val task = AppUtil.getTaskFromBundle(bundle)
        createNotificationChannel(context)
        showNotification(context, task)
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        with(NotificationManagerCompat.from(context)) {
            if (getNotificationChannel(CHANNEL_ID) == null){
                createNotificationChannel(channel)
            }
        }
    }

    private fun showNotification(context: Context, task: Task?) {
        Log.d(TAG, "showNotification: entry")
        if (task == null) return

        val notificationId = System.currentTimeMillis().toInt()
        val priority =
            if (task.important) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH
        val actionTitle = context.getString(R.string.mark_completed)

        val intent = Intent(context, MarkCompletedService::class.java)
        val bundle = Bundle().also {
            it.putSerializable("task", task)
            it.putInt("id", notificationId)
        }
        intent.putExtras(bundle)
        val actionIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(task.title)
            .setContentText(task.desc)
            .setStyle(BigTextStyle().bigText(task.desc))
            .setPriority(priority)
            .setAutoCancel(true)
            .addAction(R.drawable.round_done_48, actionTitle, actionIntent)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
                startService(context, task)
                Log.d(TAG, "showNotification: notification shown")
            }
        }
    }

    private fun startService(context: Context, task: Task) {
        val intent = Intent(context, ScheduleTaskService::class.java)
        val bundle = Bundle().also { it.putSerializable("task", task) }
        intent.putExtras(bundle)
        context.startForegroundService(intent)
    }
}