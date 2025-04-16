package ie.setu.elaine.di

import android.content.Context
import ie.setu.elaine.data.local.AppDatabase
import ie.setu.elaine.data.local.dao.RoutineDao
import ie.setu.elaine.data.local.dao.TaskDao
import ie.setu.elaine.data.repository.RoutineRepository
import ie.setu.elaine.data.repository.TaskRepository

object AppModule {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun provideAppDatabase(): AppDatabase {
        requireInitialized()
        return AppDatabase.getDatabase(appContext)
    }

    fun provideRoutineDao(): RoutineDao {
        requireInitialized()
        return provideAppDatabase().routineDao()
    }

    fun provideTaskDao(): TaskDao {
        requireInitialized()
        return provideAppDatabase().taskDao()
    }

    fun provideRoutineRepository(): RoutineRepository {
        requireInitialized()
        return RoutineRepository(provideRoutineDao(), provideTaskDao(), appContext)
    }

    fun provideTaskRepository(): TaskRepository {
        requireInitialized()
        return TaskRepository(provideTaskDao())
    }

    private fun requireInitialized() {
        if (!::appContext.isInitialized) {
            throw IllegalStateException("AppModule must be initialized with context first")
        }
    }
}