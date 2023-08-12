package seamonster.kraken.todo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.google.firebase.Timestamp
import seamonster.kraken.todo.model.ListInfo
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.repository.TaskRepo

class TaskViewModel : ViewModel() {
    companion object {
        const val TAG = "TaskViewModel"
    }

    private val dataSource = TaskRepo()

    private val upcomingFilter = MutableLiveData(false)
    fun setUpcomingFilter(b: Boolean) {
        upcomingFilter.value = b
    }

    val currentList = MutableLiveData<ListInfo>()
    fun setCurrentList(list: ListInfo) {
        currentList.value = list
    }

    val allTasks = dataSource.getTasks()

    val importantTasks = currentList.switchMap { list ->
        upcomingFilter.switchMap { b ->
            val id = list.id!!
            if (!b) dataSource.getTasks(id, false, true)
            else dataSource.getUpcomingTasks(id, false, true)
        }
    }

    val activeTasks = currentList.switchMap { list ->
        upcomingFilter.switchMap { b ->
            val id = list.id!!
            if (!b) dataSource.getTasks(id)
            else dataSource.getUpcomingTasks(id)
        }
    }

    val completedTasks = currentList.switchMap { list ->
        upcomingFilter.switchMap { b ->
            val id = list.id!!
            if (!b) dataSource.getTasks(id, true)
            else dataSource.getUpcomingTasks(id, true)
        }
    }

    fun upsert(vararg tasks: Task) {
        tasks.forEach {
            if (it.listId.isNullOrEmpty()) { it.listId = currentList.value?.id!! }
            if (it.createdAt == null) it.createdAt = Timestamp.now().toDate().time
            dataSource.upsertTask(tasks = tasks)
        }
    }

    fun delete(vararg tasks: Task) {
        dataSource.deleteTask(tasks = tasks)
    }
}
