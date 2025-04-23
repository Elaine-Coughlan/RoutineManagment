package ie.setu.elaine.data.repository

import android.content.Context
import ie.setu.elaine.data.local.dao.CompletionRecordDao
import ie.setu.elaine.data.local.dao.StreakDao
import ie.setu.elaine.data.local.entity.CompletionRecordEntity
import ie.setu.elaine.data.local.entity.StreakEntity
import ie.setu.elaine.model.CompletionRecord
import ie.setu.elaine.model.Streak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class StreakRepository(
    private val streakDao: StreakDao,
    private val completionRecordDao: CompletionRecordDao,
    private val context: Context
) {
    fun getStreakForRoutineAsFlow(routineId: String): Flow<Streak?> {
        return streakDao.getStreakForRoutineAsFlow(routineId).map { entity ->
            entity?.toStreak()
        }
    }

    suspend fun getStreakForRoutine(routineId: String): Streak? {
        return streakDao.getStreakForRoutine(routineId)?.toStreak()
    }

    fun getCompletionRecordsForRoutineAsFlow(routineId: String): Flow<List<CompletionRecord>> {
        return completionRecordDao.getCompletionRecordsForRoutineAsFlow(routineId).map { entities ->
            entities.map { it.toCompletionRecord() }
        }
    }

    suspend fun markRoutineAsCompleted(routineId: String) {
        val today = LocalDate.now()
        val todayString = today.format(Streak.DATE_FORMATTER)

        // Check if already completed today
        val existingRecord = completionRecordDao.getCompletionRecordForDate(routineId, todayString)
        if (existingRecord != null) {
            // Already completed today
            return
        }

        // Get current streak
        var streak = streakDao.getStreakForRoutine(routineId)?.toStreak() ?: Streak(
            routineId = routineId,
            lastCompletedDate = today.minusDays(1) // Assume starting streak
        )

        // Calculate new streak
        if (streak.lastCompletedDate == null) {
            // First time completion
            streak = streak.copy(
                currentStreak = 1,
                longestStreak = 1,
                lastCompletedDate = today,
                daysCompleted = 1
            )
        } else {
            val lastCompleted = streak.lastCompletedDate
            val daysCompleted = streak.daysCompleted + 1

            when {
                // Completed yesterday - continue streak
                lastCompleted == today.minusDays(1) -> {
                    val newCurrentStreak = streak.currentStreak + 1
                    streak = streak.copy(
                        currentStreak = newCurrentStreak,
                        longestStreak = maxOf(newCurrentStreak, streak.longestStreak),
                        lastCompletedDate = today,
                        daysCompleted = daysCompleted
                    )
                }
                // Completed today - no change
                lastCompleted == today -> {
                    // No change needed
                }
                // Missed days - reset streak
                else -> {
                    streak = streak.copy(
                        currentStreak = 1,
                        lastCompletedDate = today,
                        daysCompleted = daysCompleted
                    )
                }
            }
        }

        // Save completion record
        val record = CompletionRecordEntity(
            routineId = routineId,
            completedDate = todayString,
            isStreakSaver = false
        )
        completionRecordDao.insertCompletionRecord(record)

        // Update streak
        saveStreak(streak)
    }

    suspend fun useStreakSaver(routineId: String): Boolean {
        val streak = streakDao.getStreakForRoutine(routineId)?.toStreak() ?: return false

        // Check if streak saver can be used
        if (!streak.canUseStreakSaver()) {
            return false
        }

        val yesterday = LocalDate.now().minusDays(1)
        val yesterdayString = yesterday.format(Streak.DATE_FORMATTER)

        // Create a completion record for yesterday with streak saver flag
        val record = CompletionRecordEntity(
            routineId = routineId,
            completedDate = yesterdayString,
            isStreakSaver = true
        )
        completionRecordDao.insertCompletionRecord(record)

        // Update streak entity
        val updatedStreak = streak.copy(
            lastCompletedDate = yesterday,
            streakSaverUsed = true,
            streakSaverAvailable = false
        )
        saveStreak(updatedStreak)

        return true
    }

    suspend fun resetStreakSaver(routineId: String) {
        val streak = streakDao.getStreakForRoutine(routineId)?.toStreak() ?: return

        // Reset streak saver availability (e.g., weekly reset)
        val updatedStreak = streak.copy(
            streakSaverAvailable = true
        )
        saveStreak(updatedStreak)
    }

    suspend fun updateStreakGoal(routineId: String, newGoal: Int) {
        val streak = streakDao.getStreakForRoutine(routineId)?.toStreak() ?: Streak(
            routineId = routineId,
            streakGoal = newGoal
        )

        val updatedStreak = streak.copy(streakGoal = newGoal)
        saveStreak(updatedStreak)
    }

    private suspend fun saveStreak(streak: Streak) {
        val entity = streak.toEntity()
        streakDao.insertStreak(entity)
    }

    // Extension functions to convert between domain and entity models
    private fun StreakEntity.toStreak(): Streak {
        val lastCompleted = if (lastCompletedDate.isNotEmpty()) {
            LocalDate.parse(lastCompletedDate, Streak.DATE_FORMATTER)
        } else {
            null
        }

        return Streak(
            id = id,
            routineId = routineId,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletedDate = lastCompleted,
            streakGoal = streakGoal,
            streakSaverUsed = streakSaverUsed,
            streakSaverAvailable = streakSaverAvailable,
            daysCompleted = daysCompleted
        )
    }

    private fun Streak.toEntity(): StreakEntity {
        val lastCompletedDateString = lastCompletedDate?.format(Streak.DATE_FORMATTER) ?: ""

        return StreakEntity(
            id = id,
            routineId = routineId,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletedDate = lastCompletedDateString,
            streakGoal = streakGoal,
            streakSaverUsed = streakSaverUsed,
            streakSaverAvailable = streakSaverAvailable,
            daysCompleted = daysCompleted
        )
    }

    private fun CompletionRecordEntity.toCompletionRecord(): CompletionRecord {
        return CompletionRecord(
            id = id,
            routineId = routineId,
            completedDate = LocalDate.parse(completedDate, Streak.DATE_FORMATTER),
            isStreakSaver = isStreakSaver
        )
    }
}