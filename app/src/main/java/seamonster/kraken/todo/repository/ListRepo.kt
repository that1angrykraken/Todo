package seamonster.kraken.todo.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import seamonster.kraken.todo.model.ListInfo

class ListRepo {
    companion object {
        const val TAG = "ListRepo"
    }

    private val currentUserEmail = Firebase.auth.currentUser?.email
    private val collectionReference = DataSource.listReference(currentUserEmail)

    fun getAll(): MutableLiveData<List<ListInfo>> {
        val mutableLiveData = MutableLiveData<List<ListInfo>>()
        if (currentUserEmail != null) {
            collectionReference
                .addSnapshotListener { snapshot, error ->
                    if (error != null) Log.e(TAG, "getAll: Error getting data", error)
                    if (snapshot != null) {
                        val data = ArrayList<ListInfo>()
                        snapshot.documents.forEach { doc ->
                            val list = doc.toObject(ListInfo::class.java)
                            list?.let { it.id = doc.id }
                            data.add(list!!)
                        }
                        mutableLiveData.value = data
                        Log.d(TAG, "getAll: Documents fetched")
                    }
                }
        }
        return mutableLiveData
    }

    fun upsertList(list: ListInfo) {
        (if (list.id == null) collectionReference.add(list)
        else collectionReference.document(list.id!!).set(list))
            .addOnFailureListener {
                Log.e(TAG, "upsertList: Failed to upsert: ${list.id}", it)
            }

    }

    fun deleteList(list: ListInfo) {
        collectionReference.document(list.id!!).delete()
            .addOnFailureListener {
                Log.e(TAG, "upsertList: Failed to upsert: ${list.id}", it)
            }
    }
}