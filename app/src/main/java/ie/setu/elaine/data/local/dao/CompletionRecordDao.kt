package ie.setu.elaine.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ie.setu.elaine.data.local.entity.CompletionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionRecordDao {
    @Query("SELECT * FROM completion_records WHERE routineId = :routineId ORDER BY completedDate DESC")
    fun getCompletionRecordsForRoutineAsFlow(routineId: String): Flow<List<CompletionRecordEntity>>

    @Query("SELECT * FROM completion_records WHERE routineId = :routineId AND completedDate = :date")
    suspend fun getCompletionRecordForDate(routineId: String, date: String): CompletionRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletionRecord(record: CompletionRecordEntity): Long

    @Query("DELETE FROM completion_records WHERE routineId = :routineId")
    suspend fun deleteCompletionRecordsForRoutine(routineId: String)

    @Query("SELECT COUNT(*) FROM completion_records WHERE routineId = :routineId")
    suspend fun getCompletionCountForRoutine(routineId: String): Int

    @Query("SELECT COUNT(*) FROM completion_records WHERE routineId = :routineId AND isStreakSaver = 0")
    suspend fun getActualCompletionCountForRoutine(routineId: String): Int
}