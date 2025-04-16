package ie.setu.elaine

import android.app.Application
import ie.setu.elaine.data.local.AppDatabase
import ie.setu.elaine.data.repository.AchievementRepository
import ie.setu.elaine.data.repository.RoutineRepository
import ie.setu.elaine.data.repository.TaskRepository
import ie.setu.elaine.di.AppModule
import ie.setu.elaine.notifications.ReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RMApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val routineRepository by lazy { RoutineRepository(database.routineDao(), database.taskDao(), this ) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val achievementRepository by lazy { AchievementRepository(database.achievementDao()) }

    val reminderManager by lazy { ReminderManager(this) }

    override fun onCreate() {
        super.onCreate()
        AppModule.initialize(this)
        //Daily checks
        reminderManager.scheduleDailyReminder()

        // Initialize  achievements
        CoroutineScope(Dispatchers.IO).launch {
            achievementRepository.initializeAchievements()
        }
    }
}