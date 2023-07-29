package seamonster.kraken.todo.listener

import seamonster.kraken.todo.model.Task

interface TaskItemListener {
    fun onItemClick(task: Task)
    fun onItemCompletedChanged(task: Task)
    fun onItemImportantChanged(task: Task)
}