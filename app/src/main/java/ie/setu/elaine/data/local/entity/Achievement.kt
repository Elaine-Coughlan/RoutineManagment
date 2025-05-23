package ie.setu.elaine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long? = null
)