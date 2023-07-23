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
import seamonster.kraken.todo.model.TaskList

class AppViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG: String = "AppViewModel"
    }

    private val dataSource: TaskDao
    val lists: LiveData<List<TaskList>>
    var lastAction: Int = 1
    val currentList = MutableLiveData(1)

    init {
        val db = AppDatabase.getInstance(application)
        dataSource = db.taskDao()
        lists = dataSource.getAllLists()
    }

    fun importantTasks(): LiveData<List<Task>> = currentList.switchMap {
        Log.d(TAG, "importantTasks: updated")
        dataSource.getImportant(it)
    }

    fun activeTasks(): LiveData<List<Task>> = currentList.switchMap {
        Log.d(TAG, "activeTasks: updated")
        dataSource.getActiveTasks(it)
    }

    fun completedTasks(): LiveData<List<Task>> = currentList.switchMap {
        Log.d(TAG, "completedTasks: updated")
        dataSource.getCompletedTasks(it)
    }

    fun setCurrentList(id: Int){
        currentList.value = id
        Log.d(TAG, "setCurrentList: ${currentList.value}")
    }

    fun upsert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        task.listId = currentList.value!!
        dataSource.upsert(task)
        Log.d(TAG, "upsert: task upserted at ${task.listId}")
    }

    fun delete(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        task.listId = currentList.value!!
        dataSource.delete(task)
        Log.d(TAG, "delete: task deleted at ${task.listId}")
    }

    fun upsertList(list: TaskList) = viewModelScope.launch(Dispatchers.IO) {
        dataSource.upsertList(list)
        Log.d(TAG, "upsertList: updated: id = ${list.id} , name = ${list.name}")
    }
}