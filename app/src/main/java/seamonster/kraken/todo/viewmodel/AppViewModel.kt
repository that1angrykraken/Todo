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
import seamonster.kraken.todo.repository.AppDatabase
import seamonster.kraken.todo.repository.TaskDao
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.model.ListInfo

class AppViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG: String = "AppViewModel"
    }

    private val dataSource: TaskDao
    val lists: LiveData<List<ListInfo>>
    var lastAction: Int = 1
    val currentList = MutableLiveData(1)
    var upcomingFilterEnabled = MutableLiveData(false)

    init {
        val db = AppDatabase.getInstance(application)
        dataSource = db.taskDao()
        upsertList(ListInfo(1))
        lists = dataSource.getAllLists()
    }

    val importantTasks: LiveData<List<Task>> = currentList.switchMap { listId ->
        upcomingFilterEnabled.switchMap {
            if (!it) dataSource.getTasks(listId, 0, 1)
            else dataSource.getTasksSorted(listId, 0, 1)
        }
    }

    val activeTasks: LiveData<List<Task>> = currentList.switchMap { listId ->
        upcomingFilterEnabled.switchMap {
            if (!it) dataSource.getTasks(listId, 0)
            else dataSource.getTasksSorted(listId, 0)
        }
    }

    val completedTasks: LiveData<List<Task>> = currentList.switchMap { listId ->
        dataSource.getTasks(listId, 1)
    }


    fun setCurrentList(id: Int) {
        currentList.value = id
        Log.d(TAG, "setCurrentList: ${currentList.value}")
    }

    fun upsert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        task.listId = currentList.value!!
        dataSource.upsert(task)
        Log.d(TAG, "upsert: task upserted at ${task.listId}")
    }

    fun delete(vararg tasks: Task) = viewModelScope.launch(Dispatchers.IO) {
        dataSource.delete(tasks = tasks)
    }

    fun upsertList(list: ListInfo) = viewModelScope.launch(Dispatchers.IO) {
        dataSource.upsertList(list)
        Log.d(TAG, "upsertList: updated: id = ${list.id} , name = ${list.name}")
    }

    fun deleteList(list: ListInfo) = viewModelScope.launch(Dispatchers.IO) {
        val t1 = dataSource.getTasks(list.id, 0).value
        val t2 = dataSource.getTasks(list.id, 1).value
        if (!t1.isNullOrEmpty()) dataSource.delete(*t1.toTypedArray())
        if (!t2.isNullOrEmpty()) dataSource.delete(*t2.toTypedArray())
        dataSource.deleteList(list)
    }
}