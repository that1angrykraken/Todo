package seamonster.kraken.todo.viewmodel

import androidx.lifecycle.ViewModel
import seamonster.kraken.todo.model.TasksList
import seamonster.kraken.todo.repository.ListRepo
import seamonster.kraken.todo.repository.TaskRepo

class ListViewModel: ViewModel() {
    companion object{
        const val TAG = "ListViewModel"
    }

    private val dataSource = ListRepo.getInstance()

    val getLists = dataSource.getAll()

    fun upsert(list: TasksList){
        dataSource.upsertList(list)
    }

    fun delete(list: TasksList){
        TaskRepo().delete(list.id!!)
        dataSource.deleteList(list)
    }
}