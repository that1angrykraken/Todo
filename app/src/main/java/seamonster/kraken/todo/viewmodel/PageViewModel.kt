package seamonster.kraken.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.google.firebase.Timestamp
import seamonster.kraken.todo.R
import seamonster.kraken.todo.model.TasksList
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.repository.ListRepo
import seamonster.kraken.todo.repository.TaskRepo

class PageViewModel(private val application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "TaskViewModel"
    }

    private val dataSource = TaskRepo()

    private val upcomingFilter = MutableLiveData(false)
    fun setUpcomingFilter(b: Boolean) {
        upcomingFilter.value = b
    }

    var currentTask = Task()
        get() {
            if (field.listId.isNullOrEmpty()) {
                field.listId = currentList.value!!.id
            }
            return field
        }
        set(value) {
            if (value.listId.isNullOrEmpty()) {
                value.listId = currentList.value!!.id
            }
            field = value
        }

    val currentList = MutableLiveData(TasksList().apply {
        id = "0"
        name = application.getString(R.string.my_tasks)
        ListRepo().upsertList(this)
    })

    fun setCurrentList(list: TasksList) {
        currentList.value = list
    }

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

    fun anyDuplicatedDateTimeTasks(): Boolean {
        return activeTasks.value?.any {
            it.year == currentTask.year &&
            it.month == currentTask.month &&
            it.date == currentTask.date &&
            it.hour == currentTask.hour &&
            it.minute == currentTask.minute &&
            it.id != currentTask.id
        } ?: false
    }

    fun upsert(vararg tasks: Task) {
        tasks.forEach {
            if (it.listId.isNullOrEmpty()) {
                it.listId = currentList.value?.id!!
                it.createdAt = System.currentTimeMillis()
            }
            if (it.title.isBlank()) it.title = application.getString(R.string.default_task_title)
        }
        dataSource.upsert(tasks = tasks)
    }

    fun delete(vararg tasks: Task) {
        dataSource.delete(tasks = tasks)
    }
}
