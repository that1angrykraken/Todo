package seamonster.kraken.todo.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val currentUser = Firebase.auth.currentUser
                if (currentUser != null) {
                    context.startForegroundService(Intent(context, ScheduleTaskService::class.java))
                }
            }
        }
    }
}