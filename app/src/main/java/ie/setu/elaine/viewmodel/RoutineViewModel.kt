package ie.setu.elaine.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ie.setu.elaine.data.local.AppDatabase
import ie.setu.elaine.data.repository.RoutineRepository
import ie.setu.elaine.data.repository.TaskRepository
import ie.setu.elaine.model.Routine
import ie.setu.elaine.model.Task
import kotlinx.coroutines.launch

class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoutineRepository
    private val taskRepository: TaskRepository

    // Timer ViewModels
    private val taskTimerViewModel = TimerViewModel()
    private val routineTimerViewModel = TimerViewModel()

    private val _routines = mutableStateListOf<Routine>()
    val routines: List<Routine> = _routines

    // Current states for editing
    private val _currentRoutine = mutableStateOf<Routine?>(null)
    val currentRoutine: State<Routine?> = _currentRoutine

    private val _currentTask = mutableStateOf<Task?>(null)
    val currentTask: State<Task?> = _currentTask

    // Timer states - forwarded from timer view models
    val isRoutineTimerRunning: State<Boolean> = routineTimerViewModel.isTimerRunning
    val currentTaskTimerRunning: State<Boolean> = taskTimerViewModel.isTimerRunning
    val remainingRoutineTimeInSeconds: State<Int> = routineTimerViewModel.remainingTimeInSeconds
    val remainingTaskTimeInSeconds: State<Int> = taskTimerViewModel.remainingTimeInSeconds

    private val context = getApplication<Application>().applicationContext

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RoutineRepository(
            database.routineDao(), database.taskDao(), context
        )
        taskRepository = TaskRepository(database.taskDao())

        // Load routines when ViewModel is initialized
        viewModelScope.launch {
            repository.getAllRoutinesWithTasks().collect { loadedRoutines ->
                _routines.clear()
                _routines.addAll(loadedRoutines)
            }
        }
    }

    // Functions to manage routines
    fun addRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.insertRoutine(routine)
        }
    }

    fun updateRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.updateRoutine(routine)
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            repository.deleteRoutine(routineId)
        }
    }

    // Functions to manage tasks within a routine
    fun addTaskToRoutine(routineId: String, task: Task) {
        viewModelScope.launch {
            taskRepository.insertTask(task, routineId)
            // Update the local state
            repository.getRoutineWithTasks(routineId)?.let { updatedRoutine ->
                val index = _routines.indexOfFirst { it.id == routineId }
                if (index >= 0) {
                    _routines[index] = updatedRoutine
                }
            }
        }
    }

    // Timer functions
    fun startRoutineTimer(routineId: String) {
        val routine = _routines.find { it.id == routineId } ?: return

        println("Starting routine timer for routine: $routineId")
        println("Found routine: ${routine.title} with ${routine.tasks.size} tasks")

        _currentRoutine.value = routine

        // Calculate total routine time
        val totalTime = calculateTotalRoutineTimeInSeconds(routine)

        // Start routine timer
        routineTimerViewModel.startTimer(totalTime) {
            // Callback when routine timer completes
            completeRoutine()
        }

        // Start with the first task if available
        if (routine.tasks.isNotEmpty()) {
            _currentTask.value = routine.tasks.first()
            startTaskTimer(routineId, routine.tasks.first().id)
        }
    }

    fun startTaskTimer(routineId: String, taskId: String) {
        val routine = _routines.find { it.id == routineId } ?: return
        val task = routine.tasks.find { it.id == taskId } ?: return

        _currentRoutine.value = routine
        _currentTask.value = task

        // Start task timer
        val taskDuration = getTaskDurationInSeconds(task)
        taskTimerViewModel.startTimer(taskDuration) {
            // Callback when task timer completes
            completeTask()
        }
    }

    // Reset the current task timer without starting it
    fun resetTaskTimer() {
        // Just reset the timer to original duration without starting
        taskTimerViewModel.resetTimer()
        println("Task timer reset to original duration (not started)")
    }

    // Helper to get task duration in seconds
    private fun getTaskDurationInSeconds(task: Task?): Int {
        if (task == null) return 0
        return if (task.durationInSeconds > 0) {
            task.durationInSeconds
        } else {
            task.durationMinutes * 60
        }
    }

    private fun calculateTotalRoutineTimeInSeconds(routine: Routine): Int {
        return routine.tasks.sumOf { getTaskDurationInSeconds(it) }
    }

    private fun completeTask() {
        // Move to next task if available
        val currentTaskIndex = _currentRoutine.value?.tasks?.indexOfFirst {
            it.id == _currentTask.value?.id
        } ?: -1

        if (currentTaskIndex >= 0 && currentTaskIndex < (_currentRoutine.value?.tasks?.size ?: 0) - 1) {
            // Move to next task
            val nextTask = _currentRoutine.value?.tasks?.get(currentTaskIndex + 1)
            _currentTask.value = nextTask
            nextTask?.let { task ->
                _currentRoutine.value?.id?.let { routineId ->
                    startTaskTimer(routineId, task.id)
                }
            }
        } else {
            // End of routine
            completeRoutine()
        }
    }

    private fun completeRoutine() {
        // Stop all timers
        taskTimerViewModel.stopTimer()
        routineTimerViewModel.stopTimer()
    }

    fun pauseTimer() {
        // Pause both timers
        taskTimerViewModel.pauseTimer()
        routineTimerViewModel.pauseTimer()
    }

    fun resumeTimer() {
        // Resume both timers
        taskTimerViewModel.resumeTimer()
        routineTimerViewModel.resumeTimer()
    }

    fun moveToNextTask() {
        _currentTask.value?.let { currentTask ->
            _currentRoutine.value?.let { routine ->
                val currentIndex = routine.tasks.indexOfFirst { it.id == currentTask.id }
                if (currentIndex < routine.tasks.size - 1) {
                    // Stop current task timer
                    taskTimerViewModel.stopTimer()

                    // Move to next task
                    val nextTask = routine.tasks[currentIndex + 1]
                    _currentTask.value = nextTask
                    startTaskTimer(routine.id, nextTask.id)
                }
            }
        }
    }

    fun moveToPreviousTask() {
        _currentTask.value?.let { currentTask ->
            _currentRoutine.value?.let { routine ->
                val currentIndex = routine.tasks.indexOfFirst { it.id == currentTask.id }
                if (currentIndex > 0) {
                    // Stop current task timer
                    taskTimerViewModel.stopTimer()

                    // Move to previous task
                    val prevTask = routine.tasks[currentIndex - 1]
                    _currentTask.value = prevTask
                    startTaskTimer(routine.id, prevTask.id)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up timer resources
        taskTimerViewModel.stopTimer()
        routineTimerViewModel.stopTimer()
    }
}

class RoutineViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoutineViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}