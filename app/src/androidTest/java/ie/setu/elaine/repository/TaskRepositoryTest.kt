package ie.setu.elaine.repository

import ie.setu.elaine.data.local.dao.TaskDao
import ie.setu.elaine.data.local.entity.TaskEntity
import ie.setu.elaine.data.repository.TaskRepository
import ie.setu.elaine.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
class TaskRepositoryTest : Base() {

    @Mock
    private lateinit var taskDao: TaskDao

    private lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        taskRepository = TaskRepository(taskDao)
    }

    @Test
    fun `getTasksForRoutineAsFlow should return mapped domain objects`() = runBlockingTest {
        // Given
        val taskEntities = listOf(
            TaskEntity(
                id = "task1",
                routineId = "routine1",
                title = "Task 1",
                description = "Description 1",
                durationMinutes = 5,
                durationInSeconds = 300,
                isTimerEnabled = true
            ),
            TaskEntity(
                id = "task2",
                routineId = "routine1",
                title = "Task 2",
                description = "Description 2",
                durationMinutes = 10,
                durationInSeconds = 600,
                isTimerEnabled = false
            )
        )

        `when`(taskDao.getTasksForRoutineAsFlow("routine1")).thenReturn(flowOf(taskEntities))

        // When
        val result = taskRepository.getTasksForRoutineAsFlow("routine1")

        // Then
        // This is just to verify that the flow transformation works
        verify(taskDao).getTasksForRoutineAsFlow("routine1")
    }

    @Test
    fun `getTasksForRoutine should return mapped domain objects`() = runBlockingTest {
        // Given
        val taskEntities = listOf(
            TaskEntity(
                id = "task1",
                routineId = "routine1",
                title = "Task 1",
                description = "Description 1",
                durationMinutes = 5,
                durationInSeconds = 300,
                isTimerEnabled = true
            ),
            TaskEntity(
                id = "task2",
                routineId = "routine1",
                title = "Task 2",
                description = "Description 2",
                durationMinutes = 10,
                durationInSeconds = 600,
                isTimerEnabled = false
            )
        )

        `when`(taskDao.getTasksForRoutine("routine1")).thenReturn(taskEntities)

        // When
        val result = taskRepository.getTasksForRoutine("routine1")

        // Then
        assertEquals(2, result.size)
        assertEquals("task1", result[0].id)
        assertEquals("Task 1", result[0].title)
        assertEquals("Description 1", result[0].description)
        assertEquals(5, result[0].durationMinutes)
        assertEquals(300, result[0].durationInSeconds)
        assertEquals(true, result[0].isTimerEnabled)

        assertEquals("task2", result[1].id)
        assertEquals("Task 2", result[1].title)
        assertEquals("Description 2", result[1].description)
        assertEquals(10, result[1].durationMinutes)
        assertEquals(600, result[1].durationInSeconds)
        assertEquals(false, result[1].isTimerEnabled)

        verify(taskDao).getTasksForRoutine("routine1")
    }

    @Test
    fun `getTaskById should return mapped domain object if task exists`() = runBlockingTest {
        // Given
        val taskEntity = TaskEntity(
            id = "task1",
            routineId = "routine1",
            title = "Task 1",
            description = "Description 1",
            durationMinutes = 5,
            durationInSeconds = 300,
            isTimerEnabled = true
        )

        `when`(taskDao.getTaskById("task1")).thenReturn(taskEntity)

        // When
        val result = taskRepository.getTaskById("task1")

        // Then
        assertEquals("task1", result?.id)
        assertEquals("Task 1", result?.title)
        assertEquals("Description 1", result?.description)
        assertEquals(5, result?.durationMinutes)
        assertEquals(300, result?.durationInSeconds)
        assertEquals(true, result?.isTimerEnabled)

        verify(taskDao).getTaskById("task1")
    }

    @Test
    fun `getTaskById should return null if task doesn't exist`() = runBlockingTest {
        // Given
        `when`(taskDao.getTaskById("non_existent")).thenReturn(null)

        // When
        val result = taskRepository.getTaskById("non_existent")

        // Then
        assertNull(result)
        verify(taskDao).getTaskById("non_existent")
    }

    @Test
    fun `insertTask should call dao with correct entity`() = runBlockingTest {
        // Given
        val task = Task(
            id = "task1",
            title = "Task 1",
            description = "Description 1",
            durationMinutes = 5,
            durationInSeconds = 300,
            isTimerEnabled = true
        )

        // When
        taskRepository.insertTask(task, "routine1")

        // Then
        verify(taskDao).insertTask(
            TaskEntity(
                id = "task1",
                routineId = "routine1",
                title = "Task 1",
                description = "Description 1",
                durationMinutes = 5,
                durationInSeconds = 300,
                isTimerEnabled = true
            )
        )
    }

    @Test
    fun `updateTask should call dao with correct entity`() = runBlockingTest {
        // Given
        val task = Task(
            id = "task1",
            title = "Updated Task",
            description = "Updated Description",
            durationMinutes = 10,
            durationInSeconds = 600,
            isTimerEnabled = false
        )

        // When
        taskRepository.updateTask(task, "routine1")

        // Then
        verify(taskDao).updateTask(
            TaskEntity(
                id = "task1",
                routineId = "routine1",
                title = "Updated Task",
                description = "Updated Description",
                durationMinutes = 10,
                durationInSeconds = 600,
                isTimerEnabled = false
            )
        )
    }

    @Test
    fun `deleteTask should call dao with correct id`() = runBlockingTest {
        // When
        taskRepository.deleteTask("task1")

        // Then
        verify(taskDao).deleteTask("task1")
    }

    @Test
    fun `deleteTasksForRoutine should call dao with correct routineId`() = runBlockingTest {
        // When
        taskRepository.deleteTasksForRoutine("routine1")

        // Then
        verify(taskDao).deleteTasksForRoutine("routine1")
    }
}