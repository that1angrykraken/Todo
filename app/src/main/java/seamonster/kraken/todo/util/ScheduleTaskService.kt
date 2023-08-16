package seamonster.kraken.todo.util

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import seamonster.kraken.todo.model.Task

class ScheduleTaskService : Service() {
    companion object {
        const val TAG = "ScheduleTaskService"
        const val NOTIFICATION_ID = 101101
        const val CHANNEL_ID = "BACKGROUND_SERVICE_CHANNEL"
    }

    private val path = "users/${Firebase.auth.currentUser?.email}/tasks"
    private val colRef = FirebaseFirestore.getInstance().collection(path)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification())
        val task = AppUtil.getTaskFromBundle(intent.extras)
        if (task != null) updateTask(task)
        else scheduleNextTask()
        stopSelf()
        return START_NOT_STICKY
    }

    private fun scheduleNextTask() {
        colRef
            .whereEqualTo("completed", false)
            .whereGreaterThan("year", 1)
            .orderBy("year").orderBy("month").orderBy("date")
            .orderBy("hour").orderBy("minute")
            .get().addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val task = doc.toObject(Task::class.java)
                    if (AppUtil.afterNow(task)) {
                        task?.id = doc.id
                        scheduleTask(task!!)
                        break
                    }
                }
            }
    }

    private fun scheduleTask(task: Task) {
        val bundle = Bundle().apply { putSerializable("task", task) }
        val intent = Intent(this, NotificationReceiver::class.java).apply { putExtras(bundle) }
        val operationIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            AppUtil.getDateTimeFrom(task).timeInMillis,
            operationIntent
        )
        Log.d(TAG, "scheduleTask: ${task.title} ${task.desc}")
    }

    private fun updateTask(task: Task) {
        colRef.document(task.id!!).set(task)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    scheduleNextTask()
                    Log.d(TAG, "updateTask: OK")
                }
                else Log.e(TAG, "updateTask: Failed", it.exception)
            }
    }

    private fun notification(): Notification {
        with(NotificationManagerCompat.from(this)) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            if (getNotificationChannel(CHANNEL_ID) == null) {
                createNotificationChannel(channel)
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Schedule task service")
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}