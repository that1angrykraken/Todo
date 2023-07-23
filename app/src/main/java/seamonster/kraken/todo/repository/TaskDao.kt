package seamonster.kraken.todo.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.model.TaskList

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAll(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE important = 1 AND completed = 0 AND listId = :currentListId")
    fun getImportant(currentListId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 0 AND listId = :currentListId")
    fun getActiveTasks(currentListId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 1 AND listId = :currentListId")
    fun getCompletedTasks(currentListId: Int): LiveData<List<Task>>

    @Delete suspend fun delete(vararg tasks: Task)

    @Upsert suspend fun upsert(vararg tasks: Task)

    @Query("SELECT * FROM lists")
    fun getAllLists(): LiveData<List<TaskList>>

    @Delete suspend fun deleteList(list: TaskList)

    @Upsert suspend fun upsertList(list: TaskList)
}