package seamonster.kraken.todo.listener

import seamonster.kraken.todo.model.TasksList

interface ListItemListener {
    fun onItemClicked(list: TasksList)
}