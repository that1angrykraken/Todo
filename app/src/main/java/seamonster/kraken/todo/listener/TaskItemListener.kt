package seamonster.kraken.todo.listener

import seamonster.kraken.todo.model.Task

interface TaskItemListener {
    fun onItemClick(task: Task, position: Int)
    fun onItemCompletedChanged(task: Task, position: Int)
    fun onItemImportantChanged(task: Task, position: Int)
}