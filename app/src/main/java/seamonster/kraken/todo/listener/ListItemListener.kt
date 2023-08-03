package seamonster.kraken.todo.listener

import seamonster.kraken.todo.model.ListInfo

interface ListItemListener {
    fun onItemClicked(list: ListInfo)
}