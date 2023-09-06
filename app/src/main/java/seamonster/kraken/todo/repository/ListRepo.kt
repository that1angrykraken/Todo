package seamonster.kraken.todo.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import seamonster.kraken.todo.model.TasksList
import java.util.Date

class ListRepo {
    companion object {
        const val TAG = "ListRepo"
    }

    private val currentUserEmail = Firebase.auth.currentUser?.email
    private val collectionReference = DataSource.listReference(currentUserEmail)

    fun getAll(): MutableLiveData<List<TasksList>> {
        val mutableLiveData = MutableLiveData<List<TasksList>>()
        if (currentUserEmail != null) {
            collectionReference
                .addSnapshotListener { snapshot, error ->
                    if (error != null) Log.e(TAG, "getAll: Error getting data", error)
                    if (snapshot != null) {
                        val data = ArrayList<TasksList>()
                        snapshot.documents.forEach { doc ->
                            val list = doc.toObject(TasksList::class.java)
                            if (list != null) {
                                Log.d(TAG, "getAll: id = ${doc.id}")
                                list.id = doc.id
                                data.add(list)
                            }
                        }
                        mutableLiveData.value = data
                        Log.d(TAG, "getAll: Documents fetched")
                    }
                }
        }
        return mutableLiveData
    }

    fun upsertList(list: TasksList) {
        if (list.id == null) {
            Log.d(TAG, "upsertList: id is null")
            list.id = "${Date().time}"
        }
        collectionReference.document(list.id!!).set(list)
            .addOnSuccessListener {
                Log.d(TAG, "upsertList: upserted ${list.id}")
            }
            .addOnFailureListener {
                Log.e(TAG, "upsertList: Failed to upsert: ${list.id}", it)
            }
    }

    fun deleteList(list: TasksList) {
        collectionReference.document(list.id!!).delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteList: deleted ${list.id}")
            }
            .addOnFailureListener {
                Log.e(TAG, "deleteList: Failed to upsert: ${list.id}", it)
            }
    }
}