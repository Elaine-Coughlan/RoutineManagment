package ie.setu.elaine.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val durationMinutes: Int = 0,
    val isTimerEnabled: Boolean = false
)