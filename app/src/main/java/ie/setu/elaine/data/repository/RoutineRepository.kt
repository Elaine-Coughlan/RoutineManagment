// RoutineRepository.kt
package ie.setu.elaine.data.repository

import ie.setu.elaine.data.local.dao.RoutineDao
import ie.setu.elaine.data.local.dao.TaskDao
import ie.setu.elaine.data.local.entity.RoutineEntity
import ie.setu.elaine.data.local.entity.TaskEntity
import ie.setu.elaine.model.Routine
import ie.setu.elaine.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoutineRepository(
    private val routineDao: RoutineDao,
    private val taskDao: TaskDao
) {
    // Get all routines with their tasks as a Flow
    fun getAllRoutinesWithTasks(): Flow<List<Routine>> {
        return routineDao.getAllRoutinesAsFlow().map { routineEntities ->
            routineEntities.map { routineEntity ->
                val taskEntities = taskDao.getTasksForRoutine(routineEntity.id)
                convertToRoutine(routineEntity, taskEntities)
            }
        }
    }

    // Get a single routine with its tasks
    suspend fun getRoutineWithTasks(routineId: String): Routine? {
        val routineEntity = routineDao.getRoutineById(routineId) ?: return null
        val taskEntities = taskDao.getTasksForRoutine(routineId)
        return convertToRoutine(routineEntity, taskEntities)
    }

    // Insert a new routine with its tasks
    suspend fun insertRoutine(routine: Routine) {
        val routineEntity = RoutineEntity(
            id = routine.id,
            title = routine.title,
            description = routine.description,
            totalDurationMinutes = routine.totalDurationMinutes,
            isTimerEnabled = routine.isTimerEnabled
        )
        routineDao.insertRoutine(routineEntity)

        // Insert all tasks for this routine
        routine.tasks.forEach { task ->
            val taskEntity = TaskEntity(
                id = task.id,
                routineId = routine.id,
                title = task.title,
                description = task.description,
                durationMinutes = task.durationMinutes,
                durationInSeconds = task.durationInSeconds,
                isTimerEnabled = task.isTimerEnabled
            )
            taskDao.insertTask(taskEntity)
        }
    }

    // Update an existing routine and its tasks
    suspend fun updateRoutine(routine: Routine) {
        val routineEntity = RoutineEntity(
            id = routine.id,
            title = routine.title,
            description = routine.description,
            totalDurationMinutes = routine.totalDurationMinutes,
            isTimerEnabled = routine.isTimerEnabled
        )
        routineDao.updateRoutine(routineEntity)

        // Delete existing tasks and insert updated ones
        taskDao.deleteTasksForRoutine(routine.id)
        routine.tasks.forEach { task ->
            val taskEntity = TaskEntity(
                id = task.id,
                routineId = routine.id,
                title = task.title,
                description = task.description,
                durationMinutes = task.durationMinutes,
                durationInSeconds = task.durationInSeconds,
                isTimerEnabled = task.isTimerEnabled
            )
            taskDao.insertTask(taskEntity)
        }
    }

    // Delete a routine and its tasks
    suspend fun deleteRoutine(routineId: String) {
        // With proper foreign key constraints, deleting the routine should cascade to tasks
        routineDao.deleteRoutine(routineId)
    }

    // Helper method to convert entity objects to domain model
    private fun convertToRoutine(routineEntity: RoutineEntity, taskEntities: List<TaskEntity>): Routine {
        val tasks = taskEntities.map { taskEntity ->
            Task(
                id = taskEntity.id,
                title = taskEntity.title,
                description = taskEntity.description,
                durationMinutes = taskEntity.durationMinutes,
                durationInSeconds = taskEntity.durationInSeconds,
                isTimerEnabled = taskEntity.isTimerEnabled
            )
        }

        return Routine(
            id = routineEntity.id,
            title = routineEntity.title,
            description = routineEntity.description,
            tasks = tasks,
            totalDurationMinutes = routineEntity.totalDurationMinutes,
            isTimerEnabled = routineEntity.isTimerEnabled
        )
    }

}