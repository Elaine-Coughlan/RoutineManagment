package ie.setu.elaine.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.setu.elaine.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE routineId = :routineId")
    fun getTasksForRoutineAsFlow(routineId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE routineId = :routineId")
    suspend fun getTasksForRoutine(routineId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("DELETE FROM tasks WHERE routineId = :routineId")
    suspend fun deleteTasksForRoutine(routineId: String)
}