package seamonster.kraken.todo.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import seamonster.kraken.todo.R
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.repository.TaskRepo

class ScheduleTaskService : Service() {
    companion object {
        const val TAG = "ScheduleTaskService"
        const val NOTIFICATION_ID = 101101
        const val CHANNEL_ID = "BACKGROUND_SERVICE_CHANNEL"
    }

    private val repo = TaskRepo()

    override fun onCreate() {
        super.onCreate()
        listenToTasks()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification())
        if (Firebase.auth.currentUser == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun listenToTasks() {
        val util = AppUtil(this)
        repo.getColRef().addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "listenToTasks: err", error)
            }
            snapshot?.documentChanges?.forEach {
                val doc = it.document
                val task = doc.toObject<Task>().apply { id = doc.id }
                when (it.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        util.scheduleTask(task)
                    }
                    DocumentChange.Type.REMOVED -> {
                        util.cancelTask(task)
                    }
                }
            }
        }
    }

    private fun notification(): Notification {
        with(NotificationManagerCompat.from(this)) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.background_service_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            if (getNotificationChannel(CHANNEL_ID) == null) {
                createNotificationChannel(channel)
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.tasks_scheduler))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}