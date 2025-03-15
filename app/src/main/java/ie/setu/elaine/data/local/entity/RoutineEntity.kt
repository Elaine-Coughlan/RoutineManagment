package ie.setu.elaine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_table")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val totalDurationMinutes: Int,
    val isTimerEnabled: Boolean
)