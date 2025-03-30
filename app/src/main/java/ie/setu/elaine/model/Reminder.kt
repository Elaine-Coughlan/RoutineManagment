package ie.setu.elaine.model

import android.content.pm.PackageManager.ComponentEnabledSetting
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

data class Reminder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val routineId: String? = null,
    val title: String,
    val description: String ="",
    val time: LocalTime,
    val repeatDays: List<DayOfWeek> = emptyList(),
    val isEnabled: Boolean = true,
    val notificationType: NotificationType = NotificationType.STANDARD
)

enum class NotificationType{
    STANDARD,
    URGENT,
    SILENT
}