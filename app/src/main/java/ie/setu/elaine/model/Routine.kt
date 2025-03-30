package ie.setu.elaine.model

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val tasks: List<Task> = emptyList(),
    val totalDurationMinutes: Int = 0,
    val isTimerEnabled: Boolean = false,

    // New reminder fields
    val hasReminder: Boolean = false,
    val reminderTime: LocalTime? = null,
    val reminderDays: List<DayOfWeek> = listOf(DayOfWeek.MONDAY),
    val notificationType: NotificationType = NotificationType.STANDARD
)