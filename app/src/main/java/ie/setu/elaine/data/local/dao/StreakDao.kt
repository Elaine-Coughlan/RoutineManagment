package ie.setu.elaine.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ie.setu.elaine.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks WHERE routineId = :routineId")
    fun getStreakForRoutineAsFlow(routineId: String): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks WHERE routineId = :routineId")
    suspend fun getStreakForRoutine(routineId: String): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakEntity): Long

    @Update
    suspend fun updateStreak(streak: StreakEntity)

    @Query("DELETE FROM streaks WHERE routineId = :routineId")
    suspend fun deleteStreakForRoutine(routineId: String)

    @Query("SELECT * FROM streaks")
    fun getAllStreaksAsFlow(): Flow<List<StreakEntity>>
}