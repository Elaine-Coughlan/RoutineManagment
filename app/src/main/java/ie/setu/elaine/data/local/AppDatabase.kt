package ie.setu.elaine.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ie.setu.elaine.data.local.dao.RoutineDao
import ie.setu.elaine.data.local.dao.TaskDao
import ie.setu.elaine.data.local.entity.RoutineEntity
import ie.setu.elaine.data.local.entity.TaskEntity

@Database(entities = [RoutineEntity::class, TaskEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "routine_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}