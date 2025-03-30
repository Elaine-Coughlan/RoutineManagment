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
import ie.setu.elaine.model.NotificationType
import ie.setu.elaine.model.Routine
import ie.setu.elaine.model.Task
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

open class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoutineRepository
    private val taskRepository: TaskRepository

    private val _routines = mutableStateListOf<Routine>()
    var routines: List<Routine> = _routines

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

    // Jobs for timer coroutines
    private var routineTimerJob: Job? = null
    private var taskTimerJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RoutineRepository(database.routineDao(), database.taskDao())
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

        //Debugging
        println("Starting routine timer for routine: $routineId") // Add this log
        println("Found routine: ${routine.title} with ${routine.tasks.size} tasks")


        _currentRoutine.value = routine
        _remainingRoutineTimeInSeconds.value = calculateTotalRoutineTimeInSeconds(routine)
        _isRoutineTimerRunning.value = true

        // Start with the first task if available
        if (routine.tasks.isNotEmpty()) {
            _currentTask.value = routine.tasks.first()
            _remainingTaskTimeInSeconds.value = getTaskDurationInSeconds(_currentTask.value)
            _currentTaskTimerRunning.value = true
        }

        // Start routine timer
        routineTimerJob = viewModelScope.launch {
            while (_remainingRoutineTimeInSeconds.value > 0 && isActive) {
                delay(1000)
                _remainingRoutineTimeInSeconds.value -= 1
            }

            if (_remainingRoutineTimeInSeconds.value <= 0) {
                completeRoutine()
            }
        }

        // Start task timer
        startTaskTimer(routineId, routine.tasks.firstOrNull()?.id ?: "")
    }

    fun startTaskTimer(routineId: String, taskId: String) {
        val routine = _routines.find { it.id == routineId } ?: return
        val task = routine.tasks.find { it.id == taskId } ?: return

        _currentRoutine.value = routine
        _currentTask.value = task
        _remainingTaskTimeInSeconds.value = getTaskDurationInSeconds(task)
        _currentTaskTimerRunning.value = true

        taskTimerJob?.cancel()
        taskTimerJob = viewModelScope.launch {
            while (_remainingTaskTimeInSeconds.value > 0 && isActive) {
                delay(1000)
                _remainingTaskTimeInSeconds.value -= 1
            }

            if (_remainingTaskTimeInSeconds.value <= 0) {
                completeTask()
            }
        }
    }

    // Helper to get task duration in seconds, preferring durationInSeconds field
    // but calculating from minutes if needed
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
        _currentTaskTimerRunning.value = false

        // Move to next task if available
        val currentTaskIndex = _currentRoutine.value?.tasks?.indexOfFirst {
            it.id == _currentTask.value?.id
        } ?: -1

        if (currentTaskIndex >= 0 && currentTaskIndex < (_currentRoutine.value?.tasks?.size
                ?: 0) - 1
        ) {
            // Move to next task
            val nextTask = _currentRoutine.value?.tasks?.get(currentTaskIndex + 1)
            _currentTask.value = nextTask
            _remainingTaskTimeInSeconds.value = getTaskDurationInSeconds(nextTask)
            _currentTaskTimerRunning.value = true
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
        _isRoutineTimerRunning.value = false
        _currentTaskTimerRunning.value = false
        routineTimerJob?.cancel()
        taskTimerJob?.cancel()
    }

    fun pauseTimer() {
        _isRoutineTimerRunning.value = false
        _currentTaskTimerRunning.value = false
        routineTimerJob?.cancel()
        taskTimerJob?.cancel()
    }

    fun resumeTimer() {
        if (_currentTask.value != null) {
            _currentTaskTimerRunning.value = true
            _isRoutineTimerRunning.value = true

            _currentRoutine.value?.id?.let { routineId ->
                _currentTask.value?.id?.let { taskId ->
                    startTaskTimer(routineId, taskId)
                }
            }

            // Restart the routine timer
            routineTimerJob = viewModelScope.launch {
                while (_remainingRoutineTimeInSeconds.value > 0 && isActive) {
                    delay(1000)
                    _remainingRoutineTimeInSeconds.value -= 1
                }

                if (_remainingRoutineTimeInSeconds.value <= 0) {
                    completeRoutine()
                }
            }
        }
    }

    fun moveToNextTask() {
        _currentTask.value?.let { currentTask ->
            _currentRoutine.value?.let { routine ->
                val currentIndex = routine.tasks.indexOfFirst { it.id == currentTask.id }
                if (currentIndex < routine.tasks.size - 1) {
                    taskTimerJob?.cancel()
                    val nextTask = routine.tasks[currentIndex + 1]
                    _currentTask.value = nextTask
                    _remainingTaskTimeInSeconds.value = getTaskDurationInSeconds(nextTask)
                    _currentTaskTimerRunning.value = true
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
                    taskTimerJob?.cancel()
                    val prevTask = routine.tasks[currentIndex - 1]
                    _currentTask.value = prevTask
                    _remainingTaskTimeInSeconds.value = getTaskDurationInSeconds(prevTask)
                    _currentTaskTimerRunning.value = true
                    startTaskTimer(routine.id, prevTask.id)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        routineTimerJob?.cancel()
        taskTimerJob?.cancel()
    }

   //Handling reminders
   fun updateRoutineReminder(
       routineId: String,
       hasReminder: Boolean,
       reminderTime: LocalTime? = null,
       reminderDays: List<DayOfWeek> = emptyList(),
       notificationType: NotificationType = NotificationType.STANDARD
   ) {
       routines = routines.map { routine ->
           if (routine.id == routineId) {
               routine.copy(
                   hasReminder = hasReminder,
                   reminderTime = reminderTime,
                   reminderDays = reminderDays,
                   notificationType = notificationType
               )
           } else routine
       }
   }

    //Help to interact with Reminder viewModel
    fun syncReminderWithRoutine(
        routineId: String,
        reminderViewModel: ReminderViewModel
    ) {
        val routine = routines.firstOrNull { it.id == routineId } ?: return

        if (routine.hasReminder && routine.reminderTime != null) {
            // Create or update reminder
            reminderViewModel.createReminder(
                title = routine.title,
                description = "Routine: ${routine.title}",
                time = routine.reminderTime,
                repeatDays = routine.reminderDays,
                routineId = routineId
                // notificationType parameter will be added in ReminderViewModel update
            )
        } else {
            // If routine exists in the reminders list
            val existingReminder = reminderViewModel.reminders.value.firstOrNull { it.routineId == routineId }
            if (existingReminder != null) {
                reminderViewModel.deleteReminder(existingReminder.id)
            }
        }
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