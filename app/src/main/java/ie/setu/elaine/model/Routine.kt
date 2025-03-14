package ie.setu.elaine.model

import java.util.UUID

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val tasks: List<Task> = emptyList(),
    val totalDurationMinutes: Int = 0,
    val isTimerEnabled: Boolean = false
)