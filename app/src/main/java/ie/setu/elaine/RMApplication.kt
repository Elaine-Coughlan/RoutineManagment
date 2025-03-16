package ie.setu.elaine

import android.app.Application
import ie.setu.elaine.data.local.AppDatabase
import ie.setu.elaine.data.repository.RoutineRepository
import ie.setu.elaine.data.repository.TaskRepository

class RMApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val routineRepository by lazy { RoutineRepository(database.routineDao(), database.taskDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
}