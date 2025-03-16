package ie.setu.elaine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// RoutineEntity.kt
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val totalDurationMinutes: Int,
    val isTimerEnabled: Boolean
)
