package ie.setu.elaine.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ie.setu.elaine.model.NotificationType
import ie.setu.elaine.model.Reminder
import ie.setu.elaine.service.ReminderService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class ReminderViewModel(private val application: Application, private val reminderService: ReminderService) : ViewModel() {
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders

    fun createReminder(
        title: String,
        description: String = "",
        time: LocalTime,
        repeatDays: List<DayOfWeek> = listOf(DayOfWeek.MONDAY),
        routineId: String? = null,
        notificationType: NotificationType = NotificationType.STANDARD
    ) {
        val newReminder = Reminder(
            title = title,
            description = description,
            time = time,
            repeatDays = repeatDays,
            routineId = routineId,
            notificationType = notificationType
        )

        viewModelScope.launch {
            reminderService.scheduleReminder(newReminder)
            _reminders.value += newReminder
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            reminderService.cancelReminder(reminderId)
            _reminders.value = _reminders.value.filter { it.id != reminderId }
        }
    }

    fun updateReminderStatus(reminderId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            _reminders.value = _reminders.value.map { reminder ->
                if (reminder.id == reminderId) {
                    val updatedReminder = reminder.copy(isEnabled = isEnabled)
                    if (isEnabled) {
                        reminderService.scheduleReminder(updatedReminder)
                    } else {
                        reminderService.cancelReminder(reminderId)
                    }
                    updatedReminder
                } else reminder
            }
        }
    }

}

class ReminderViewModelFactory(private val application: Application, private val reminderService: ReminderService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(application, reminderService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}