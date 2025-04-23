package ie.setu.elaine.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streaks",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId")]
)
data class StreakEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletedDate: String, // ISO-8601 format (YYYY-MM-DD)
    val streakGoal: Int, // 21/30/66 days goal
    val streakSaverUsed: Boolean,
    val streakSaverAvailable: Boolean,
    val daysCompleted: Int // Total days the routine has been completed
)