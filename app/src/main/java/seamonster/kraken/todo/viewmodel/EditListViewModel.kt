package seamonster.kraken.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import seamonster.kraken.todo.R
import seamonster.kraken.todo.model.TasksList
import seamonster.kraken.todo.repository.ListRepo

class EditListViewModel(private val application: Application) : AndroidViewModel(application) {
    private val repo = ListRepo()
    private val lists = repo.getAll()
    var currentList = TasksList()

    fun upsert() {
        val newName = rename(currentList.name)
        currentList.name = newName
        repo.upsertList(currentList)
    }

    private fun rename(listName: String?, count: Int = 1): String {
        return if (listName.isNullOrEmpty()) {
            val renamed = application.getString(R.string.default_list_name)
            rename(renamed)
        } else {
            if (lists.value != null && lists.value!!.any { it.name == listName && it.id != currentList.id }) { // rename if exists
                if (count == 1) {
                    rename("$listName $count", count + 1)
                } else {
                    val oldValue = "${count - 1}"
                    rename(listName.replace(oldValue, "$count"), count + 1)
                }
            } else listName
        }
    }
}