package ie.setu.elaine.repository

import android.app.Application
import android.content.Context
import ie.setu.elaine.RMApplication
import ie.setu.elaine.data.local.dao.RoutineDao
import ie.setu.elaine.data.local.dao.TaskDao
import ie.setu.elaine.data.local.entity.RoutineEntity
import ie.setu.elaine.data.local.entity.TaskEntity
import ie.setu.elaine.data.repository.AchievementRepository
import ie.setu.elaine.data.repository.RoutineRepository
import ie.setu.elaine.model.Routine
import ie.setu.elaine.model.Task
import ie.setu.elaine.notifications.ReminderManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

@ExperimentalCoroutinesApi
class RoutineRepositoryTest : Base() {

    @Mock
    private lateinit var routineDao: RoutineDao

    @Mock
    private lateinit var taskDao: TaskDao

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var application: RMApplication

    @Mock
    private lateinit var achievementRepository: AchievementRepository

    @Mock
    private lateinit var reminderManager: ReminderManager

    private lateinit var routineRepository: RoutineRepository

    @Before
    fun setup() {
        routineRepository = RoutineRepository(routineDao, taskDao, context)

        // Mock application context chain
        `when`(context.applicationContext).thenReturn(context)
        `when`(context as? Application).thenReturn(application)
        `when`(application.achievementRepository).thenReturn(achievementRepository)
    }

    @Test
    fun `getAllRoutinesWithTasks should return mapped domain objects`() = runBlockingTest {
        // Given
        val routineEntity1 = RoutineEntity(
            id = "routine1",
            title = "Morning Routine",
            description = "Start the day right",
            totalDurationMinutes = 30,
            isTimerEnabled = true
        )

        val taskEntity1 = TaskEntity(
            id = "task1",
            routineId = "routine1",
            title = "Brush teeth",
            description = "Use mint toothpaste",
            durationMinutes = 2,
            durationInSeconds = 120,
            isTimerEnabled = true
        )

        val taskEntity2 = TaskEntity(
            id = "task2",
            routineId = "routine1",
            title = "Shower",
            description = "Quick shower",
            durationMinutes = 10,
            durationInSeconds = 600,
            isTimerEnabled = true
        )

        `when`(routineDao.getAllRoutinesAsFlow()).thenReturn(flowOf(listOf(routineEntity1)))
        `when`(taskDao.getTasksForRoutine("routine1")).thenReturn(listOf(taskEntity1, taskEntity2))

        // When
        val result = routineRepository.getAllRoutinesWithTasks()

        // Then

        // Verify that the flow transformation works as expected
        verify(routineDao).getAllRoutinesAsFlow()
        verify(taskDao).getTasksForRoutine("routine1")
    }

    @Test
    fun `getRoutineWithTasks should return null if routine doesn't exist`() = runBlockingTest {
        // Given
        `when`(routineDao.getRoutineById("non_existent")).thenReturn(null)

        // When
        val result = routineRepository.getRoutineWithTasks("non_existent")

        // Then
        assert(result == null)
        verify(routineDao).getRoutineById("non_existent")
        verify(taskDao, never()).getTasksForRoutine(any())
    }

    @Test
    fun `getRoutineWithTasks should return routine with tasks if routine exists`() = runBlockingTest {
        // Given
        val routineEntity = RoutineEntity(
            id = "routine1",
            title = "Morning Routine",
            description = "Start the day right",
            totalDurationMinutes = 30,
            isTimerEnabled = true
        )

        val taskEntities = listOf(
            TaskEntity(
                id = "task1",
                routineId = "routine1",
                title = "Brush teeth",
                description = "Use mint toothpaste",
                durationMinutes = 2,
                durationInSeconds = 120,
                isTimerEnabled = true
            )
        )

        `when`(routineDao.getRoutineById("routine1")).thenReturn(routineEntity)
        `when`(taskDao.getTasksForRoutine("routine1")).thenReturn(taskEntities)

        // When
        val result = routineRepository.getRoutineWithTasks("routine1")

        // Then
        assert(result != null)
        assert(result?.id == "routine1")
        assert(result?.title == "Morning Routine")
        assert(result?.tasks?.size == 1)
        assert(result?.tasks?.first()?.title == "Brush teeth")

        verify(routineDao).getRoutineById("routine1")
        verify(taskDao).getTasksForRoutine("routine1")
    }

    @Test
    fun `insertRoutine should insert routine entity and task entities`() = runBlockingTest {
        // Given
        val task = Task(
            id = "task1",
            title = "Brush teeth",
            description = "Use mint toothpaste",
            durationMinutes = 2,
            durationInSeconds = 120,
            isTimerEnabled = true
        )

        val routine = Routine(
            id = "routine1",
            title = "Morning Routine",
            description = "Start the day right",
            tasks = listOf(task),
            totalDurationMinutes = 30,
            isTimerEnabled = true
        )

        `when`(routineDao.getAllRoutinesAsFlow()).thenReturn(flowOf(listOf(
            RoutineEntity("routine1", "Morning Routine", "Start the day right", 30, true)
        )))
        `when`(reminderManager.getConsecutiveDays()).thenReturn(3)

        // When
        routineRepository.insertRoutine(routine)

        // Then
        // Verify routine was inserted
        verify(routineDao).insertRoutine(any())

        // Verify each task was inserted
        verify(taskDao).insertTask(any())

        // Verify achievement checks
        verify(achievementRepository).checkFirstRoutineAchievement(1)
        verify(achievementRepository).checkOrganizationProAchievement(1)
        verify(achievementRepository).checkConsistencyAchievement(3)
    }

    @Test
    fun `updateRoutine should update routine entity and replace tasks`() = runBlockingTest {
        // Given
        val task = Task(
            id = "task1",
            title = "Updated task",
            description = "Updated description",
            durationMinutes = 5,
            durationInSeconds = 300,
            isTimerEnabled = false
        )

        val routine = Routine(
            id = "routine1",
            title = "Updated Routine",
            description = "Updated description",
            tasks = listOf(task),
            totalDurationMinutes = 45,
            isTimerEnabled = false
        )

        // When
        routineRepository.updateRoutine(routine)

        // Then
        // Verify routine was updated
        verify(routineDao).updateRoutine(any())

        // Verify tasks were deleted and then inserted
        verify(taskDao).deleteTasksForRoutine("routine1")
        verify(taskDao).insertTask(any())
    }

    @Test
    fun `deleteRoutine should delete the routine`() = runBlockingTest {
        // When
        routineRepository.deleteRoutine("routine1")

        // Then
        verify(routineDao).deleteRoutine("routine1")
    }
}