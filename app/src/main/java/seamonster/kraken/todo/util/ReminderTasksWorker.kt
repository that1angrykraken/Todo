package seamonster.kraken.todo.util

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import seamonster.kraken.todo.R
import seamonster.kraken.todo.activity.MainActivity
import seamonster.kraken.todo.repository.DataSource
import java.util.Calendar

class ReminderTasksWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    companion object {
        const val TAG = "ReminderTasksWorker"
        const val CHANNEL_ID = "REMINDER_CHANNEL"
        const val NOTIFICATION_ID = 563900001
    }

    private val currentUserEmail = Firebase.auth.currentUser?.email
    private val colRef = DataSource.taskReference(currentUserEmail)

    override fun doWork(): Result {
        countTomorrowTasks()
        return Result.success()
    }

    private fun countTomorrowTasks() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DATE, get(Calendar.DATE) + 1)
        }
        colRef
            .whereEqualTo("year", calendar.get(Calendar.YEAR))
            .whereEqualTo("month", calendar.get(Calendar.MONTH))
            .whereEqualTo("date", calendar.get(Calendar.DATE))
            .whereEqualTo("completed", false)
            .get().addOnSuccessListener {
                val counter = it.documents.count()
                showNotification(counter)
            }
    }

    private fun showNotification(counter: Int) {
        val title =
            if (counter > 0) context.getString(R.string.reminder_content_title, counter)
            else context.getString(R.string.reminder_content_title_no_tasks)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.round_task_alt_48)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.remider_content_text))
            .setContentIntent(contentIntent())
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannelCompat
                    .Builder(CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
                    .setName(context.getString(R.string.reminder_channel_name))
                    .build()
                createNotificationChannel(channel)
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun contentIntent() : PendingIntent{
        val bundle = Bundle().apply { putBoolean("upcoming", true) }
        val intent = Intent(context, MainActivity::class.java).apply { putExtras(bundle) }
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(context, 1, intent, flags)
    }
}