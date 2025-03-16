// TaskRepository.kt
package ie.setu.elaine.data.repository

import ie.setu.elaine.data.local.dao.TaskDao
import ie.setu.elaine.data.local.entity.TaskEntity
import ie.setu.elaine.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val taskDao: TaskDao) {
    // Get all tasks for a specific routine as a Flow
    fun getTasksForRoutineAsFlow(routineId: String): Flow<List<Task>> {
        return taskDao.getTasksForRoutineAsFlow(routineId).map { taskEntities ->
            taskEntities.map { taskEntity ->
                convertToTask(taskEntity)
            }
        }
    }

    // Get all tasks for a specific routine
    suspend fun getTasksForRoutine(routineId: String): List<Task> {
        return taskDao.getTasksForRoutine(routineId).map { taskEntity ->
            convertToTask(taskEntity)
        }
    }

    // Get a specific task by ID
    suspend fun getTaskById(taskId: String): Task? {
        val taskEntity = taskDao.getTaskById(taskId) ?: return null
        return convertToTask(taskEntity)
    }

    // Insert a new task
    suspend fun insertTask(task: Task, routineId: String) {
        val taskEntity = TaskEntity(
            id = task.id,
            routineId = routineId,
            title = task.title,
            description = task.description,
            durationMinutes = task.durationMinutes,
            durationInSeconds = task.durationInSeconds,
            isTimerEnabled = task.isTimerEnabled
        )
        taskDao.insertTask(taskEntity)
    }

    // Update an existing task
    suspend fun updateTask(task: Task, routineId: String) {
        val taskEntity = TaskEntity(
            id = task.id,
            routineId = routineId,
            title = task.title,
            description = task.description,
            durationMinutes = task.durationMinutes,
            durationInSeconds = task.durationInSeconds,
            isTimerEnabled = task.isTimerEnabled
        )
        taskDao.updateTask(taskEntity)
    }

    // Delete a task
    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId)
    }

    // Delete all tasks for a routine
    suspend fun deleteTasksForRoutine(routineId: String) {
        taskDao.deleteTasksForRoutine(routineId)
    }

    // Helper method to convert entity to domain model
    private fun convertToTask(taskEntity: TaskEntity): Task {
        return Task(
            id = taskEntity.id,
            title = taskEntity.title,
            description = taskEntity.description,
            durationMinutes = taskEntity.durationMinutes,
            durationInSeconds = taskEntity.durationInSeconds,
            isTimerEnabled = taskEntity.isTimerEnabled
        )
    }
}