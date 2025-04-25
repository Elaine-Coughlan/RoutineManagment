package ie.setu.elaine.repository

import ie.setu.elaine.data.repository.AchievementRepository
import ie.setu.elaine.data.local.dao.AchievementDao
import ie.setu.elaine.data.local.entity.Achievement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

@ExperimentalCoroutinesApi
class AchievementRepositoryTest : Base() {

    @Mock
    private lateinit var achievementDao: AchievementDao

    private lateinit var achievementRepository: AchievementRepository

    @Before
    fun setup() {
        achievementRepository = AchievementRepository(achievementDao)
    }

    @Test
    fun `initializeAchievements should insert default achievements that don't exist yet`() = runBlockingTest {
        // Given
        `when`(achievementDao.getAchievementById("first_routine")).thenReturn(null)
        `when`(achievementDao.getAchievementById("task_master")).thenReturn(null)
        `when`(achievementDao.getAchievementById("organisation_pro")).thenReturn(
            Achievement("organisation_pro", "Organisation Pro", "Created 5 routines", true)
        )

        // When
        achievementRepository.initializeAchievements()

        // Then
        verify(achievementDao).getAchievementById("first_routine")
        verify(achievementDao).getAchievementById("task_master")
        verify(achievementDao).getAchievementById("organisation_pro")

        // Should insert first_routine and task_master since they don't exist
        verify(achievementDao).insertAchievement(
            Achievement("first_routine", "First Routine", "Created your first routine", false)
        )
        verify(achievementDao).insertAchievement(
            Achievement("task_master", "Task Master", "Completed 10 tasks", false)
        )

        // Should not insert organisation_pro since it already exists
        verify(achievementDao, never()).insertAchievement(
            Achievement("organisation_pro", "Organisation Pro", "Created 5 routines", false)
        )
    }

    @Test
    fun `unlockAchievement should update achievement status if achievement exists and is not unlocked`() = runBlockingTest {
        // Given
        val achievement = Achievement("first_routine", "First Routine", "Created your first routine", false)
        `when`(achievementDao.getAchievementById("first_routine")).thenReturn(achievement)

        // When
        achievementRepository.unlockAchievement("first_routine")

        // Then
        verify(achievementDao).updateAchievementStatus(
            id = "first_routine",
            isUnlocked = true,
            unlockedDate = any()
        )
    }

    @Test
    fun `unlockAchievement should not update if achievement is already unlocked`() = runBlockingTest {
        // Given
        val achievement = Achievement("first_routine", "First Routine", "Created your first routine", true)
        `when`(achievementDao.getAchievementById("first_routine")).thenReturn(achievement)

        // When
        achievementRepository.unlockAchievement("first_routine")

        // Then
        verify(achievementDao, never()).updateAchievementStatus(
            id = any(),
            isUnlocked = anyBoolean(),
            unlockedDate = any()
        )
    }

    @Test
    fun `unlockAchievement should not update if achievement doesn't exist`() = runBlockingTest {
        // Given
        `when`(achievementDao.getAchievementById("non_existent")).thenReturn(null)

        // When
        achievementRepository.unlockAchievement("non_existent")

        // Then
        verify(achievementDao, never()).updateAchievementStatus(
            id = any(),
            isUnlocked = anyBoolean(),
            unlockedDate = any()
        )
    }

    @Test
    fun `checkFirstRoutineAchievement should unlock achievement when routineCount is 1 or more`() = runBlockingTest {
        // Given
        val achievement = Achievement("first_routine", "First Routine", "Created your first routine", false)
        `when`(achievementDao.getAchievementById("first_routine")).thenReturn(achievement)

        // When
        achievementRepository.checkFirstRoutineAchievement(1)

        // Then
        verify(achievementDao).updateAchievementStatus(
            id = "first_routine",
            isUnlocked = true,
            unlockedDate = any()
        )
    }

    @Test
    fun `checkFirstRoutineAchievement should not unlock achievement when routineCount is 0`() = runBlockingTest {
        // When
        achievementRepository.checkFirstRoutineAchievement(0)

        // Then
        verify(achievementDao, never()).updateAchievementStatus(
            id = any(),
            isUnlocked = anyBoolean(),
            unlockedDate = any()
        )
    }

    @Test
    fun `checkOrganizationProAchievement should unlock achievement when routineCount is 5 or more`() = runBlockingTest {
        // Given
        val achievement = Achievement("organization_pro", "Organisation Pro", "Created 5 routines", false)
        `when`(achievementDao.getAchievementById("organization_pro")).thenReturn(achievement)

        // When
        achievementRepository.checkOrganizationProAchievement(5)

        // Then
        verify(achievementDao).updateAchievementStatus(
            id = "organization_pro",
            isUnlocked = true,
            unlockedDate = any()
        )
    }

    @Test
    fun `checkOrganizationProAchievement should not unlock achievement when routineCount is less than 5`() = runBlockingTest {
        // When
        achievementRepository.checkOrganizationProAchievement(4)

        // Then
        verify(achievementDao, never()).updateAchievementStatus(
            id = any(),
            isUnlocked = anyBoolean(),
            unlockedDate = any()
        )
    }

    @Test
    fun `checkConsistencyAchievement should unlock achievement when consecutiveDays is 7 or more`() = runBlockingTest {
        // Given
        val achievement = Achievement("consistency", "Consistency", "Completed routines for 7 consecutive days", false)
        `when`(achievementDao.getAchievementById("consistency")).thenReturn(achievement)

        // When
        achievementRepository.checkConsistencyAchievement(7)

        // Then
        verify(achievementDao).updateAchievementStatus(
            id = "consistency",
            isUnlocked = true,
            unlockedDate = any()
        )
    }

    @Test
    fun `checkConsistencyAchievement should not unlock achievement when consecutiveDays is less than 7`() = runBlockingTest {
        // When
        achievementRepository.checkConsistencyAchievement(6)

        // Then
        verify(achievementDao, never()).updateAchievementStatus(
            id = any(),
            isUnlocked = anyBoolean(),
            unlockedDate = any()
        )
    }

    @Test
    fun `checkAndUpdateAchievements should check all achievements with given criteria`() = runBlockingTest {
        // Given
        val firstRoutineAchievement = Achievement("first_routine", "First Routine", "Created your first routine", false)
        val organizationProAchievement = Achievement("organization_pro", "Organization Pro", "Created 5 routines", false)
        val consistencyAchievement = Achievement("consistency", "Consistency", "Completed routines for 7 consecutive days", false)

        `when`(achievementDao.getAchievementById("first_routine")).thenReturn(firstRoutineAchievement)
        `when`(achievementDao.getAchievementById("organization_pro")).thenReturn(organizationProAchievement)
        `when`(achievementDao.getAchievementById("consistency")).thenReturn(consistencyAchievement)

        // When
        achievementRepository.checkAndUpdateAchievements(
            routineCount = 5,
            taskCompletedCount = 8,
            hasDoneTimedRoutine = true,
            consecutiveDays = 7
        )

        // Then
        verify(achievementDao).updateAchievementStatus(
            id = "first_routine",
            isUnlocked = true,
            unlockedDate = any()
        )
        verify(achievementDao).updateAchievementStatus(
            id = "organization_pro",
            isUnlocked = true,
            unlockedDate = any()
        )
        verify(achievementDao).updateAchievementStatus(
            id = "consistency",
            isUnlocked = true,
            unlockedDate = any()
        )
    }
}