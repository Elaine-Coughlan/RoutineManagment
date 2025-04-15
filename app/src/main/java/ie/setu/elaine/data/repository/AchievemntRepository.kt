package ie.setu.elaine.data.repository

import ie.setu.elaine.data.local.dao.AchievementDao
import ie.setu.elaine.data.local.entity.Achievement
import kotlinx.coroutines.flow.Flow

class AchievementRepository(private val achievementDao: AchievementDao) {

    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()

    suspend fun initializeAchievements(){

        val defaultAchievements = listOf(
            Achievement(
                id = "first_routine",
                title = "First Routine",
                description = "Created your first routine",
                isUnlocked = false
            ),
            Achievement(
                id = "task_master",
                title = "Task Master",
                description = "Completed 10 tasks",
                isUnlocked = false
            ),
            Achievement(
                id = "consistency",
                title = "Consistency",
                description = "Used the app for 7 days in a row",
                isUnlocked = false
            ),
            Achievement(
                id = "time_keeper",
                title = "Time Keeper",
                description = "Completed a timed routine",
                isUnlocked = false
            ),

            Achievement(
                id = "organisation_pro",
                title = "Organisation Pro",
                description = "Created 5 routines",
                isUnlocked = false
            ),

        )

        for (achievement in defaultAchievements){
            val exisiting = achievementDao.getAchievementById(achievement.id)
            if(exisiting == null){
                achievementDao.insertAchievement(achievement)
            }
        }
    }

    suspend fun unlockAchievement(id: String){
        achievementDao.updateAchievementStatus(
            id = id,
            isUnlocked = true,
            unlockedDate = System.currentTimeMillis()
        )
    }

    suspend fun checkAndUpdateAchievements(
        routineCount: Int,
        taskCompletedCount: Int,
        hasDoneTimedRoutine: Boolean,
        consecutiveDays: Int
    ) {
        if (routineCount >= 1) {
            unlockAchievement("first_routine")
        }

        if (routineCount >= 5) {
            unlockAchievement("organization_pro")
        }

        if (taskCompletedCount >= 10) {
            unlockAchievement("task_master")
        }

        if (hasDoneTimedRoutine) {
            unlockAchievement("time_keeper")
        }

        if (consecutiveDays >= 7) {
            unlockAchievement("consistency")
        }
    }
}
