package seamonster.kraken.todo.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import seamonster.kraken.todo.model.Task
import java.util.Calendar

class TaskRepo {
    companion object {
        const val TAG = "FirestoreDatabase"
        private var _instance: TaskRepo? = null
        fun getInstance() : TaskRepo{
            if (_instance == null) _instance = TaskRepo()
            return _instance!!
        }
    }

    private val currentUserEmail = Firebase.auth.currentUser?.email
    private val collectionReference = DataSource.taskReference(currentUserEmail)

    fun getTasks(): MutableLiveData<List<Task>> {
        val mutableLiveData = MutableLiveData<List<Task>>()
        if (currentUserEmail != null) {
            collectionReference
                .whereEqualTo("completed", false)
                .orderBy("year").orderBy("month").orderBy("date")
                .orderBy("hour").orderBy("minute")
                .whereGreaterThan("year", 0)
                .addSnapshotListener(snapshotListener(mutableLiveData))
        }
        return mutableLiveData
    }

    fun getUpcomingTasks(
        listId: String,
        completed: Boolean = false,
        vararg important: Boolean = booleanArrayOf(false, true)
    ): MutableLiveData<List<Task>> {
        val mutableLiveData = MutableLiveData<List<Task>>()
        if (currentUserEmail != null) {
            collectionReference
                .whereEqualTo("listId", listId)
                .whereEqualTo("completed", completed)
                .whereIn("important", important.toList())
                .orderBy("year").orderBy("month").orderBy("date")
                .orderBy("hour").orderBy("minute")
                .whereGreaterThan("year", 0)
                .addSnapshotListener(snapshotListener(mutableLiveData))
        }
        return mutableLiveData
    }

    fun getTasks(
        listId: String,
        completed: Boolean = false,
        vararg important: Boolean = booleanArrayOf(false, true)
    ): MutableLiveData<List<Task>> {
        val mutableLiveData = MutableLiveData<List<Task>>()
        if (currentUserEmail != null) {
            collectionReference
                .whereEqualTo("listId", listId)
                .whereEqualTo("completed", completed)
                .whereIn("important", important.toList())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "getTasks: Error while listening to documents", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val data = ArrayList<Task>()
                        for (doc in snapshot.documents) {
                            val task = doc.toObject(Task::class.java)
                            task?.let { it.id = doc.id }
                            data.add(task!!)
                        }
                        mutableLiveData.value = data
                        Log.d(TAG, "getTasks: Documents fetched")
                    }
                }
        }
        return mutableLiveData
    }

    private fun snapshotListener(mutableLiveData: MutableLiveData<List<Task>>) =
        EventListener<QuerySnapshot> { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "snapshotListener: Error while listening to documents", error)
                return@EventListener
            }
            if (snapshot != null) {
                val data = ArrayList<Task>()
                for (doc in snapshot.documents) {
                    val task = doc.toObject(Task::class.java)
                    task?.let { it.id = doc.id }
                    data.add(task!!)
                }
                mutableLiveData.value = data.filter {
                    Calendar.getInstance().apply {
                        set(it.year, it.month, it.date, it.hour, it.minute, 0)
                    }.after(Calendar.getInstance())
                }
                Log.d(TAG, "snapshotListener: Documents fetched")
            }
        }

    fun upsert(vararg tasks: Task) {
        tasks.forEach {
            (if (it.id == null) collectionReference.add(it)
            else collectionReference.document("${it.id}").set(it))
                .addOnFailureListener { e ->
                    Log.e(TAG, "upsertTask: Failed to upsert task: ${it.id}", e)
                }
        }
    }

    fun delete(vararg tasks: Task) {
        tasks.forEach {
            collectionReference.document("${it.id}").delete()
                .addOnFailureListener { e ->
                    Log.e(TAG, "upsertTask: Failed to delete task: ${it.id}", e)
                }
        }
    }

    fun delete(listId: String){
        collectionReference.whereEqualTo("listId", listId).get().addOnSuccessListener {
            for (doc in it){
                val task = doc.toObject(Task::class.java)
                delete(task)
            }
        }
    }

}