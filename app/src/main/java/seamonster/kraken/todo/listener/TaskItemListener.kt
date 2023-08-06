package seamonster.kraken.todo.listener

import seamonster.kraken.todo.model.Task

interface TaskItemListener {
    fun onItemClick(task: Task)
    fun onItemCompletedChange(task: Task)
    fun onItemImportantChange(task: Task)
}