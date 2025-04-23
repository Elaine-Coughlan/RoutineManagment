package ie.setu.elaine.viewmodel

import android.annotation.SuppressLint
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
import ie.setu.elaine.data.repository.StreakRepository
import ie.setu.elaine.data.repository.TaskRepository
import ie.setu.elaine.model.CompletionRecord
import ie.setu.elaine.model.Routine
import ie.setu.elaine.model.Streak
import ie.setu.elaine.model.Task

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * ViewModel for managing routines, tasks, timers, and streaks.
 * This is the central ViewModel that handles all routine-related functionality.
 */
class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    // Repositories for data access
    private val repository: RoutineRepository
    private val taskRepository: TaskRepository
    private val streakRepository: StreakRepository

    // Timer ViewModels - one for the overall routine timer, one for the current task timer
    private val taskTimerViewModel = TimerViewModel()
    private val routineTimerViewModel = TimerViewModel()

    // List of all routines with their tasks
    private val _routines = mutableStateListOf<Routine>()
    val routines: List<Routine> = _routines

    // Current states for editing/viewing routines and tasks
    private val _currentRoutine = mutableStateOf<Routine?>(null)
    val currentRoutine: State<Routine?> = _currentRoutine

    private val _currentTask = mutableStateOf<Task?>(null)
    val currentTask: State<Task?> = _currentTask

    // Timer states - forwarded from timer view models for UI access
    val isRoutineTimerRunning: State<Boolean> = routineTimerViewModel.isTimerRunning
    val currentTaskTimerRunning: State<Boolean> = taskTimerViewModel.isTimerRunning
    val remainingRoutineTimeInSeconds: State<Int> = routineTimerViewModel.remainingTimeInSeconds
    val remainingTaskTimeInSeconds: State<Int> = taskTimerViewModel.remainingTimeInSeconds

    // Streak tracking states
    private val _currentStreak = mutableStateOf<Streak?>(null)
    val currentStreak: State<Streak?> = _currentStreak

    private val _completionRecords = mutableStateListOf<CompletionRecord>()
    val completionRecords: List<CompletionRecord> = _completionRecords

    private val _streakSaverAvailable = mutableStateOf(false)
    val streakSaverAvailable: State<Boolean> = _streakSaverAvailable

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    init {
        // Initialize repositories with database access
        val database = AppDatabase.getDatabase(application)
        repository = RoutineRepository(
            database.routineDao(), database.taskDao(), context
        )
        taskRepository = TaskRepository(database.taskDao())
        streakRepository = StreakRepository(
            database.streakDao(), database.completionRecordDao(), context
        )

        // Load all routines when ViewModel is initialized
        viewModelScope.launch {
            repository.getAllRoutinesWithTasks().collect { loadedRoutines ->
                _routines.clear()
                _routines.addAll(loadedRoutines)
            }
        }
    }

    // === ROUTINE MANAGEMENT FUNCTIONS ===

    /**
     * Adds a new routine to the database and initializes its streak tracking.
     * @param routine The routine to add
     */
    fun addRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.insertRoutine(routine)
            // Initialize streak for the new routine with default 30-day goal
            streakRepository.updateStreakGoal(routine.id, 30)
        }
    }

    /**
     * Updates an existing routine in the database.
     * @param routine The updated routine
     */
    fun updateRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.updateRoutine(routine)
        }
    }

    /**
     * Deletes a routine from the database.
     * @param routineId ID of the routine to delete
     */
    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            repository.deleteRoutine(routineId)
        }
    }

    // === TASK MANAGEMENT FUNCTIONS ===

    /**
     * Adds a task to a specific routine.
     * @param routineId The ID of the routine to add the task to
     * @param task The task to add
     */
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

    // === TIMER FUNCTIONS ===

    /**
     * Start the timer for an entire routine and its first task.
     * @param routineId ID of the routine to start
     */
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
            completeRoutine(routine.id)
        }

        // Start with the first task if available
        if (routine.tasks.isNotEmpty()) {
            _currentTask.value = routine.tasks.first()
            startTaskTimer(routineId, routine.tasks.first().id)
        }
    }

    /**
     * Start the timer for a specific task within a routine.
     * @param routineId ID of the parent routine
     * @param taskId ID of the task to start
     */
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

    /**
     * Reset the current task timer without starting it.
     */
    fun resetTaskTimer() {
        // Just reset the timer to original duration without starting
        taskTimerViewModel.resetTimer()
        println("Task timer reset to original duration (not started)")
    }

    // Helper function to get task duration in seconds
    private fun getTaskDurationInSeconds(task: Task?): Int {
        if (task == null) return 0
        return if (task.durationInSeconds > 0) {
            task.durationInSeconds
        } else {
            task.durationMinutes * 60
        }
    }

    // Calculate the total time for a routine based on all its tasks
    private fun calculateTotalRoutineTimeInSeconds(routine: Routine): Int {
        return routine.tasks.sumOf { getTaskDurationInSeconds(it) }
    }

    /**
     * Handle completion of the current active task.
     * Either moves to the next task or completes the routine if it was the last task.
     */
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
            _currentRoutine.value?.id?.let { routineId ->
                completeRoutine(routineId)
            }
        }
    }

    /**
     * Mark a routine as completed.
     * This is the key function for completing a routine and updating streak data.
     * @param routineId ID of the routine to mark as completed
     */
    private fun completeRoutine(routineId: String) {
        // Stop all timers
        taskTimerViewModel.stopTimer()
        routineTimerViewModel.stopTimer()

        // Mark routine as completed for streak tracking
        viewModelScope.launch {
            streakRepository.markRoutineAsCompleted(routineId)
            loadStreakData(routineId)
        }
    }

    /**
     * Manually mark a routine as completed regardless of timer status.
     * @param routineId ID of the routine to mark as completed
     */
    fun manuallyCompleteRoutine(routineId: String) {
        completeRoutine(routineId)
    }

    /**
     * Pause both the routine and current task timers.
     */
    fun pauseTimer() {
        // Pause both timers
        taskTimerViewModel.pauseTimer()
        routineTimerViewModel.pauseTimer()
    }

    /**
     * Resume both the routine and current task timers.
     */
    fun resumeTimer() {
        // Resume both timers
        taskTimerViewModel.resumeTimer()
        routineTimerViewModel.resumeTimer()
    }

    /**
     * Move to the next task in the routine sequence.
     */
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

    /**
     * Move to the previous task in the routine sequence.
     */
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

    // === STREAK TRACKING FUNCTIONS ===

    /**
     * Load streak and completion data for a specific routine.
     * @param routineId ID of the routine to load streak data for
     */
    fun loadStreakData(routineId: String) {
        viewModelScope.launch {
            // Load streak data for the selected routine
            streakRepository.getStreakForRoutine(routineId)?.let { streak ->
                _currentStreak.value = streak
                _streakSaverAvailable.value = streak.canUseStreakSaver()
            }

            // Load completion records
            streakRepository.getCompletionRecordsForRoutineAsFlow(routineId).collectLatest { records ->
                _completionRecords.clear()
                _completionRecords.addAll(records)
            }
        }
    }

    /**
     * Update the streak goal for a routine.
     * @param routineId ID of the routine to update
     * @param newGoal New goal value in days
     */
    fun updateStreakGoal(routineId: String, newGoal: Int) {
        viewModelScope.launch {
            streakRepository.updateStreakGoal(routineId, newGoal)
            loadStreakData(routineId)
        }
    }

    /**
     * Use a streak saver to maintain a streak that would otherwise be broken.
     * @param routineId ID of the routine to use streak saver on
     */
    fun useStreakSaver(routineId: String) {
        viewModelScope.launch {
            val success = streakRepository.useStreakSaver(routineId)
            if (success) {
                loadStreakData(routineId)
            }
        }
    }

    /**
     * Clean up resources when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        // Clean up timer resources
        taskTimerViewModel.stopTimer()
        routineTimerViewModel.stopTimer()
    }
}

/**
 * Factory for creating RoutineViewModel instances.
 */
class RoutineViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoutineViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}