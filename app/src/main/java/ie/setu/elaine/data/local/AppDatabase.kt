package ie.setu.elaine.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ie.setu.elaine.data.local.dao.AchievementDao
import ie.setu.elaine.data.local.dao.RoutineDao
import ie.setu.elaine.data.local.dao.TaskDao
import ie.setu.elaine.data.local.entity.Achievement
import ie.setu.elaine.data.local.entity.RoutineEntity
import ie.setu.elaine.data.local.entity.TaskEntity

@Database(entities = [RoutineEntity::class, TaskEntity::class, Achievement::class], version = 5, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun taskDao(): TaskDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "routine_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}