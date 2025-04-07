package ie.setu.elaine

import android.app.Application
import ie.setu.elaine.data.local.AppDatabase
import ie.setu.elaine.data.repository.RoutineRepository
import ie.setu.elaine.data.repository.TaskRepository
import ie.setu.elaine.notifications.ReminderManager

class RMApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val routineRepository by lazy { RoutineRepository(database.routineDao(), database.taskDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }

    val reminderManager by lazy { ReminderManager(this) }

    override fun onCreate() {
        super.onCreate()
        //Daily checks
        reminderManager.scheduleDailyReminder()
    }
}