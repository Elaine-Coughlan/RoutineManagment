package ie.setu.elaine.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ie.setu.elaine.model.Routine
import ie.setu.elaine.model.Task

class TaskViewModel : ViewModel() {
    private val _routines = mutableStateListOf<Routine>()
    val routines: List<Routine> = _routines

    // Current states for editing
    private val _currentRoutine = mutableStateOf<Routine?>(null)
    val currentRoutine: State<Routine?> = _currentRoutine

    private val _currentTask = mutableStateOf<Task?>(null)
    val currentTask: State<Task?> = _currentTask

    // Timer states
    private val _isRoutineTimerRunning = mutableStateOf(false)
    val isRoutineTimerRunning: State<Boolean> = _isRoutineTimerRunning

    private val _currentTaskTimerRunning = mutableStateOf(false)
    val currentTaskTimerRunning: State<Boolean> = _currentTaskTimerRunning

    private val _remainingRoutineTimeInSeconds = mutableStateOf(0)
    val remainingRoutineTimeInSeconds: State<Int> = _remainingRoutineTimeInSeconds

    private val _remainingTaskTimeInSeconds = mutableStateOf(0)
    val remainingTaskTimeInSeconds: State<Int> = _remainingTaskTimeInSeconds

    // Functions to manage routines
    fun addRoutine(routine: Routine) {
        _routines.add(routine)
    }

    fun updateRoutine(routine: Routine) {
        val index = _routines.indexOfFirst { it.id == routine.id }
        if (index >= 0) {
            _routines[index] = routine
        }
    }

    fun deleteRoutine(routineId: String) {
        _routines.removeIf { it.id == routineId }
    }

    // Functions to manage tasks within a routine
    fun addTaskToRoutine(routineId: String, task: Task) {
        val index = _routines.indexOfFirst { it.id == routineId }
        if (index >= 0) {
            val routine = _routines[index]
            val updatedTasks = routine.tasks + task
            _routines[index] = routine.copy(tasks = updatedTasks)
        }
    }

    // Functions for timer control
    fun startRoutineTimer(routineId: String) {
        //TODO Implementation for starting the routine timer
    }

    fun startTaskTimer(routineId: String, taskId: String) {
        //TODO Implementation for starting a task timer
    }

}