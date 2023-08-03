package seamonster.kraken.todo.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.model.ListInfo
import java.util.Calendar

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE completed = 0 AND dateTime >= :currentDateTime")
    fun getAll(currentDateTime: Calendar = Calendar.getInstance()): LiveData<List<Task>>

    @Query(
        "SELECT * FROM tasks WHERE listId = :listId AND important IN (:important) " +
                "AND completed = :completed ORDER BY id DESC"
    )
    fun getTasks(
        listId: Int,
        completed: Int,
        vararg important: Int = intArrayOf(0, 1)
    ): LiveData<List<Task>>

    @Query(
        "SELECT * FROM tasks WHERE listId = :listId AND important IN (:important) " +
                "AND completed = :completed AND dateTime >= :currentDateTime ORDER BY dateTime"
    )
    fun getTasksSorted(
        listId: Int,
        completed: Int,
        currentDateTime: Calendar,
        vararg important: Int = intArrayOf(0, 1)
    ): LiveData<List<Task>>

    @Query(
        "SELECT * FROM tasks WHERE completed = 0 AND dateTime >= :currentDateTime " +
                "ORDER BY dateTime LIMIT 1"
    )
    fun getUpcomingTask(currentDateTime: Calendar): Task?

    @Query("SELECT * FROM tasks WHERE id = :id ")
    fun getTaskById(id: Int): Task

    @Delete
    suspend fun delete(vararg tasks: Task)

    @Upsert
    suspend fun upsert(vararg tasks: Task)

    @Query("SELECT * FROM lists ORDER BY id ASC")
    fun getAllLists(): LiveData<List<ListInfo>>

    @Delete
    suspend fun deleteList(list: ListInfo)

    @Upsert
    suspend fun upsertList(list: ListInfo)
}