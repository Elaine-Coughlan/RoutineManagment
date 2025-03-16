package ie.setu.elaine.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// TaskEntity.kt
@Entity(
    tableName = "tasks",
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
data class TaskEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val durationInSeconds: Int,
    val isTimerEnabled: Boolean
)