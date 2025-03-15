package ie.setu.elaine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class TaskEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val isTimerEnabled: Boolean,
    val orderInRoutine: Int

)