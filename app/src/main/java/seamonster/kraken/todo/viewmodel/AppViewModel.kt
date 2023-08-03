package seamonster.kraken.todo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import seamonster.kraken.todo.persistence.AppDatabase
import seamonster.kraken.todo.persistence.TaskDao
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.model.ListInfo
import java.util.Calendar

class AppViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG: String = "AppViewModel"
    }

    private val dataSource: TaskDao
    val lists: LiveData<List<ListInfo>>
    val currentList = MutableLiveData<ListInfo>()
    var upcomingFilterEnabled = MutableLiveData(false)

    init {
        val db = AppDatabase.getInstance(application)
        dataSource = db.taskDao()
        lists = dataSource.getAllLists()
    }

    val allTasks: LiveData<List<Task>> = dataSource.getAll()

    val importantTasks: LiveData<List<Task>> = currentList.switchMap { list ->
        upcomingFilterEnabled.switchMap {
            val currentDateTime =
                Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
            if (!it) dataSource.getTasks(list.id, 0, 1)
            else dataSource.getTasksSorted(list.id, 0, currentDateTime, 1)
        }
    }

    val activeTasks: LiveData<List<Task>> = currentList.switchMap { list ->
        upcomingFilterEnabled.switchMap {
            val currentDateTime =
                Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
            if (!it) dataSource.getTasks(list.id, 0)
            else dataSource.getTasksSorted(list.id, 0, currentDateTime)
        }
    }

    val completedTasks: LiveData<List<Task>> = currentList.switchMap { list ->
        dataSource.getTasks(list.id, 1)
    }

    fun setCurrentList(list: ListInfo) {
        currentList.value = list
        Log.d(TAG, "setCurrentList: ${currentList.value?.id}")
    }

    fun upsert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        task.listId = currentList.value!!.id
        dataSource.upsert(task)
    }

    fun delete(vararg tasks: Task) = viewModelScope.launch(Dispatchers.IO) {
        dataSource.delete(tasks = tasks)
    }

    fun upsertList(list: ListInfo) = viewModelScope.launch(Dispatchers.IO) {
        dataSource.upsertList(list)
    }

    fun deleteList(list: ListInfo) = viewModelScope.launch(Dispatchers.IO) {
        val t1 = dataSource.getTasks(list.id, 0).value
        val t2 = dataSource.getTasks(list.id, 1).value
        if (!t1.isNullOrEmpty()) dataSource.delete(*t1.toTypedArray())
        if (!t2.isNullOrEmpty()) dataSource.delete(*t2.toTypedArray())
        dataSource.deleteList(list)
    }
}