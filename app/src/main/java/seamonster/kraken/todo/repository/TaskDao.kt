package seamonster.kraken.todo.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.model.ListInfo

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE listId = :listId AND important IN (:important) AND completed = :completed " +
            "ORDER BY id DESC")
    fun getTasks(listId: Int, completed: Int, vararg important: Int = intArrayOf(0, 1)): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE listId = :listId AND important IN (:important) AND completed = :completed " +
            "AND datetime(year, month, date, hour, minute) >= date('now') ORDER BY year, month, date, hour, minute")
    fun getTasksSorted(listId: Int, completed: Int, vararg important: Int = intArrayOf(0, 1)): LiveData<List<Task>>

    @Delete
    suspend fun delete(vararg tasks: Task)

    @Upsert
    suspend fun upsert(vararg tasks: Task)

    @Query("SELECT * FROM lists")
    fun getAllLists(): LiveData<List<ListInfo>>

    @Delete
    suspend fun deleteList(list: ListInfo)

    @Upsert
    suspend fun upsertList(list: ListInfo)
}