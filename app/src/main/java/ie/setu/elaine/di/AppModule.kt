package ie.setu.elaine.di

import android.content.Context
import ie.setu.elaine.data.local.AppDatabase
import ie.setu.elaine.data.local.dao.RoutineDao
import ie.setu.elaine.data.local.dao.TaskDao
import ie.setu.elaine.data.repository.RoutineRepository
import ie.setu.elaine.data.repository.TaskRepository

object AppModule {
    fun provideAppDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    fun provideRoutineDao(database: AppDatabase): RoutineDao {
        return database.routineDao()
    }

    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    fun provideRoutineRepository(routineDao: RoutineDao, taskDao: TaskDao): RoutineRepository {
        return RoutineRepository(routineDao, taskDao)
    }

    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepository(taskDao)
    }
}