package seamonster.kraken.todo.viewmodel

import androidx.lifecycle.ViewModel
import seamonster.kraken.todo.model.ListInfo
import seamonster.kraken.todo.repository.ListRepo

class ListViewModel: ViewModel() {
    companion object{
        const val TAG = "ListViewModel"
    }

    private val dataSource = ListRepo()

    val getLists = dataSource.getAll()

    fun upsert(list: ListInfo){
        dataSource.upsertList(list)
    }

    fun delete(list: ListInfo){
        dataSource.deleteList(list)
    }
}