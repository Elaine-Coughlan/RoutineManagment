package ie.setu.elaine.data.model

import android.app.ActivityManager.TaskDescription
import java.util.UUID

data class Task (
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: Int = 0,
    val isTimerEnabled: Boolean = false
)