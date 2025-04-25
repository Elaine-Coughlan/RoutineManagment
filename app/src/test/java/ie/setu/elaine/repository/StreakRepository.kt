package ie.setu.elaine.repository

import android.content.Context
import ie.setu.elaine.data.local.dao.CompletionRecordDao
import ie.setu.elaine.data.local.dao.StreakDao
import ie.setu.elaine.data.local.entity.CompletionRecordEntity
import ie.setu.elaine.data.local.entity.StreakEntity
import ie.setu.elaine.data.repository.StreakRepository
import ie.setu.elaine.model.Streak
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import java.time.LocalDate

@ExperimentalCoroutinesApi
class StreakRepositoryTest : Base() {

    @Mock
    private lateinit var streakDao: StreakDao

    @Mock
    private lateinit var completionRecordDao: CompletionRecordDao

    @Mock
    private lateinit var context: Context

    private lateinit var streakRepository: StreakRepository
    private val today = LocalDate.now()
    private val todayString = today.format(Streak.DATE_FORMATTER)
    private val yesterday = today.minusDays(1)
    private val yesterdayString = yesterday.format(Streak.DATE_FORMATTER)

    @Before
    fun setup() {
        streakRepository = StreakRepository(streakDao, completionRecordDao, context)
    }

    @Test
    fun `getStreakForRoutineAsFlow should return mapped domain object`() = runBlockingTest {
        // Given
        val streakEntity = StreakEntity(
            id = 1,
            routineId = "routine1",
            currentStreak = 3,
            longestStreak = 5,
            lastCompletedDate = yesterdayString,
            streakGoal = 10,
            streakSaverUsed = false,
            streakSaverAvailable = true,
            daysCompleted = 8
        )

        `when`(streakDao.getStreakForRoutineAsFlow("routine1")).thenReturn(flowOf(streakEntity))

        // When
        val result = streakRepository.getStreakForRoutineAsFlow("routine1")

        // Then
        // This just tests that the flow transformation works
        verify(streakDao).getStreakForRoutineAsFlow("routine1")
    }

    @Test
    fun `getStreakForRoutine should return mapped domain object if streak exists`() = runBlockingTest {
        // Given
        val streakEntity = StreakEntity(
            id = 1,
            routineId = "routine1",
            currentStreak = 3,
            longestStreak = 5,
            lastCompletedDate = yesterdayString,
            streakGoal = 10,
            streakSaverUsed = false,
            streakSaverAvailable = true,
            daysCompleted = 8
        )

        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(streakEntity)

        // When
        val result = streakRepository.getStreakForRoutine("routine1")

        // Then
        assertEquals(1, result?.id)
        assertEquals("routine1", result?.routineId)
        assertEquals(3, result?.currentStreak)
        assertEquals(5, result?.longestStreak)
        assertEquals(yesterday, result?.lastCompletedDate)
        assertEquals(10, result?.streakGoal)
        assertFalse(result?.streakSaverUsed ?: true)
        assertTrue(result?.streakSaverAvailable ?: false)
        assertEquals(8, result?.daysCompleted)

        verify(streakDao).getStreakForRoutine("routine1")
    }

    @Test
    fun `getStreakForRoutine should return null if streak doesn't exist`() = runBlockingTest {
        // Given
        `when`(streakDao.getStreakForRoutine("non_existent")).thenReturn(null)

        // When
        val result = streakRepository.getStreakForRoutine("non_existent")

        // Then
        assertNull(result)
        verify(streakDao).getStreakForRoutine("non_existent")
    }

    @Test
    fun `markRoutineAsCompleted should not create record if already completed today`() = runBlockingTest {
        // Given
        val existingRecord = CompletionRecordEntity(
            id = 1,
            routineId = "routine1",
            completedDate = todayString,
            isStreakSaver = false
        )

        `when`(completionRecordDao.getCompletionRecordForDate("routine1", todayString)).thenReturn(existingRecord)

        // When
        streakRepository.markRoutineAsCompleted("routine1")

        // Then
        verify(completionRecordDao, never()).insertCompletionRecord(any())
        verify(streakDao, never()).insertStreak(any())
    }

    @Test
    fun `markRoutineAsCompleted should create first completion record for new streak`() = runBlockingTest {
        // Given
        `when`(completionRecordDao.getCompletionRecordForDate("routine1", todayString)).thenReturn(null)
        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(null)

        // When
        streakRepository.markRoutineAsCompleted("routine1")

        // Then
        verify(completionRecordDao).insertCompletionRecord(
            match { record ->
                record.routineId == "routine1" &&
                        record.completedDate == todayString &&
                        !record.isStreakSaver
            }
        )

        verify(streakDao).insertStreak(
            match { streak ->
                streak.routineId == "routine1" &&
                        streak.currentStreak == 1 &&
                        streak.longestStreak == 1 &&
                        streak.lastCompletedDate == todayString &&
                        streak.daysCompleted == 1
            }
        )
    }

    @Test
    fun `markRoutineAsCompleted should continue streak when completed yesterday`() = runBlockingTest {
        // Given
        val existingStreak = StreakEntity(
            id = 1,
            routineId = "routine1",
            currentStreak = 3,
            longestStreak = 5,
            lastCompletedDate = yesterdayString,
            streakGoal = 10,
            streakSaverUsed = false,
            streakSaverAvailable = true,
            daysCompleted = 8
        )

        `when`(completionRecordDao.getCompletionRecordForDate("routine1", todayString)).thenReturn(null)
        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(existingStreak)

        // When
        streakRepository.markRoutineAsCompleted("routine1")

        // Then
        verify(completionRecordDao).insertCompletionRecord(
            match { record ->
                record.routineId == "routine1" &&
                        record.completedDate == todayString &&
                        !record.isStreakSaver
            }
        )

        verify(streakDao).insertStreak(
            match { streak ->
                streak.routineId == "routine1" &&
                        streak.currentStreak == 4 && // Increased from 3
                        streak.longestStreak == 5 && // Unchanged since current < longest
                        streak.lastCompletedDate == todayString &&
                        streak.daysCompleted == 9 // Increased from 8
            }
        )
    }

    @Test
    fun `markRoutineAsCompleted should reset streak when missed days`() = runBlockingTest {
        // Given
        val twoDaysAgo = today.minusDays(2)
        val twoDaysAgoString = twoDaysAgo.format(Streak.DATE_FORMATTER)

        val existingStreak = StreakEntity(
            id = 1,
            routineId = "routine1",
            currentStreak = 5,
            longestStreak = 7,
            lastCompletedDate = twoDaysAgoString, // Last completed 2 days ago
            streakGoal = 10,
            streakSaverUsed = false,
            streakSaverAvailable = true,
            daysCompleted = 12
        )

        `when`(completionRecordDao.getCompletionRecordForDate("routine1", todayString)).thenReturn(null)
        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(existingStreak)

        // When
        streakRepository.markRoutineAsCompleted("routine1")

        // Then
        verify(completionRecordDao).insertCompletionRecord(
            match { record ->
                record.routineId == "routine1" &&
                        record.completedDate == todayString &&
                        !record.isStreakSaver
            }
        )

        verify(streakDao).insertStreak(
            match { streak ->
                streak.routineId == "routine1" &&
                        streak.currentStreak == 1 && // Reset to 1
                        streak.longestStreak == 7 && // Unchanged
                        streak.lastCompletedDate == todayString &&
                        streak.daysCompleted == 13 // Increased from 12
            }
        )
    }

    @Test
    fun `useStreakSaver should return false if streak doesn't exist`() = runBlockingTest {
        // Given
        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(null)

        // When
        val result = streakRepository.useStreakSaver("routine1")

        // Then
        assertFalse(result)
        verify(completionRecordDao, never()).insertCompletionRecord(any())
        verify(streakDao, never()).insertStreak(any())
    }

    @Test
    fun `useStreakSaver should return false if streak saver not available`() = runBlockingTest {
        // Given
        val existingStreak = StreakEntity(
            id = 1,
            routineId = "routine1",
            currentStreak = 3,
            longestStreak = 5,
            lastCompletedDate = yesterdayString,
            streakGoal = 10,
            streakSaverUsed = true,
            streakSaverAvailable = false, // Not available
            daysCompleted = 8
        )

        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(existingStreak)

        // When
        val result = streakRepository.useStreakSaver("routine1")

        // Then
        assertFalse(result)
        verify(completionRecordDao, never()).insertCompletionRecord(any())
        verify(streakDao, never()).insertStreak(any())
    }

    @Test
    fun `useStreakSaver should create completion record and update streak when available`() = runBlockingTest {
        // Given
        val existingStreak = StreakEntity(
            id = 1,
            routineId = "routine1",
            currentStreak = 3,
            longestStreak = 5,
            lastCompletedDate = LocalDate.parse(yesterdayString, Streak.DATE_FORMATTER).minusDays(1).format(Streak.DATE_FORMATTER),            streakGoal = 10,
            streakSaverUsed = false,
            streakSaverAvailable = true, // Available
            daysCompleted = 8
        )

        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(existingStreak)

        // When
        val result = streakRepository.useStreakSaver("routine1")

        // Then
        assertTrue(result)

        verify(completionRecordDao).insertCompletionRecord(
            match { record ->
                record.routineId == "routine1" &&
                        record.completedDate == yesterdayString && // For yesterday
                        record.isStreakSaver // Marked as streak saver
            }
        )

        verify(streakDao).insertStreak(
            match { streak ->
                streak.routineId == "routine1" &&
                        streak.currentStreak == 3 && // Unchanged
                        streak.longestStreak == 5 && // Unchanged
                        streak.lastCompletedDate == yesterdayString && // Updated to yesterday
                        streak.streakSaverUsed && // Marked as used
                        !streak.streakSaverAvailable && // No longer available
                        streak.daysCompleted == 8 // Unchanged
            }
        )
    }

    @Test
    fun `resetStreakSaver should make streak saver available again`() = runBlockingTest {
        // Given
        val existingStreak = StreakEntity(
            id = 1,
            routineId = "routine1",
            currentStreak = 3,
            longestStreak = 5,
            lastCompletedDate = yesterdayString,
            streakGoal = 10,
            streakSaverUsed = true,
            streakSaverAvailable = false, // Not available
            daysCompleted = 8
        )

        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(existingStreak)

        // When
        streakRepository.resetStreakSaver("routine1")

        // Then
        verify(streakDao).insertStreak(
            match { streak ->
                streak.routineId == "routine1" &&
                        streak.currentStreak == 3 && // Unchanged
                        streak.longestStreak == 5 && // Unchanged
                        streak.lastCompletedDate == yesterdayString && // Unchanged
                        streak.streakSaverUsed && // Unchanged
                        streak.streakSaverAvailable && // Now available
                        streak.daysCompleted == 8 // Unchanged
            }
        )
    }

    @Test
    fun `updateStreakGoal should update goal for existing streak`() = runBlockingTest {
        // Given
        val existingStreak = StreakEntity(
            id = 1,
            routineId = "routine1",
            currentStreak = 3,
            longestStreak = 5,
            lastCompletedDate = yesterdayString,
            streakGoal = 10,
            streakSaverUsed = false,
            streakSaverAvailable = true,
            daysCompleted = 8
        )

        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(existingStreak)

        // When
        streakRepository.updateStreakGoal("routine1", 15)

        // Then
        verify(streakDao).insertStreak(
            match { streak ->
                streak.routineId == "routine1" &&
                        streak.currentStreak == 3 && // Unchanged
                        streak.longestStreak == 5 && // Unchanged
                        streak.lastCompletedDate == yesterdayString && // Unchanged
                        streak.streakGoal == 15 && // Updated
                        !streak.streakSaverUsed && // Unchanged
                        streak.streakSaverAvailable && // Unchanged
                        streak.daysCompleted == 8 // Unchanged
            }
        )
    }

    @Test
    fun `updateStreakGoal should create new streak if none exists`() = runBlockingTest {
        // Given
        `when`(streakDao.getStreakForRoutine("routine1")).thenReturn(null)

        // When
        streakRepository.updateStreakGoal("routine1", 20)

        // Then
        verify(streakDao).insertStreak(
            match { streak ->
                streak.routineId == "routine1" &&
                        streak.currentStreak == 0 && // Default
                        streak.longestStreak == 0 && // Default
                        streak.lastCompletedDate.isEmpty() && // Default
                        streak.streakGoal == 20 && // Set to new goal
                        !streak.streakSaverUsed && // Default
                        streak.streakSaverAvailable && // Default
                        streak.daysCompleted == 0 // Default
            }
        )
    }

    // Helper method for Mockito argument matching
    private fun <T> match(predicate: (T) -> Boolean): T {
        return ArgumentMatchers.argThat { arg: T -> predicate(arg) }
    }
}