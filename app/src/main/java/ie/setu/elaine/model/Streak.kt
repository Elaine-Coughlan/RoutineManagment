package ie.setu.elaine.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Streak(
    val id: Long = 0,
    val routineId: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: LocalDate? = null,
    val streakGoal: Int = 30, // Default 30 days
    val streakSaverUsed: Boolean = false,
    val streakSaverAvailable: Boolean = true,
    val daysCompleted: Int = 0
) {
    fun progress(): Float {
        return if (streakGoal <= 0) 0f else currentStreak.toFloat() / streakGoal.toFloat()
    }

    fun isStreakActive(): Boolean {
        if (lastCompletedDate == null) return false

        val currentDate = LocalDate.now()
        return lastCompletedDate == currentDate || lastCompletedDate == currentDate.minusDays(1)
    }

    fun canUseStreakSaver(): Boolean {
        if (!streakSaverAvailable || streakSaverUsed) return false

        // Can only use streak saver if streak was active and we missed yesterday
        if (lastCompletedDate == null) return false

        val currentDate = LocalDate.now()
        return lastCompletedDate == currentDate.minusDays(2)
    }

    companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}

data class CompletionRecord(
    val id: Long = 0,
    val routineId: String,
    val completedDate: LocalDate,
    val isStreakSaver: Boolean = false
)