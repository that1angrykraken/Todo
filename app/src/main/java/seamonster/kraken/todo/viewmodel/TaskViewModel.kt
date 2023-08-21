package seamonster.kraken.todo.viewmodel

import android.app.Application
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import seamonster.kraken.todo.R
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.repository.TaskRepo

class TaskViewModel(private val application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "TaskViewModel"
    }

    private val dataSource = TaskRepo.getInstance()

    var currentTask = Task()

    fun upsertTask() {
        if (currentTask.title.isBlank()) {
            currentTask.title = application.getString(R.string.default_task_title)
        }
        dataSource.upsert(currentTask)
    }

    fun markCompleted(completed: Boolean) {
        currentTask.completed = completed
        dataSource.upsert(currentTask)
    }

    fun deleteTask() {
        dataSource.delete(currentTask)
    }
}

