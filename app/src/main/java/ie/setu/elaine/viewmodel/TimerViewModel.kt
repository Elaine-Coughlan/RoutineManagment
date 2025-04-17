package ie.setu.elaine.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class TimerViewModel : ViewModel() {
    // Timer states
    private val _isTimerRunning = mutableStateOf(false)
    val isTimerRunning: State<Boolean> = _isTimerRunning

    private val _remainingTimeInSeconds = mutableStateOf(0)
    val remainingTimeInSeconds: State<Int> = _remainingTimeInSeconds

    // Original duration used for reset
    private var originalDurationInSeconds = 0

    // Job for timer coroutine
    private var timerJob: Job? = null
    private val timerMutex = Mutex() // For thread safety when accessing timer job

    // Callback for when timer completes
    private var onTimerComplete: (() -> Unit)? = null

    // Debugging flag
    private val debug = true

    fun startTimer(durationInSeconds: Int, onComplete: () -> Unit) {
        // Cancel any existing timer
        stopTimerJob()

        if (debug) println("Starting timer with duration: $durationInSeconds seconds")

        // Store original duration for reset
        originalDurationInSeconds = durationInSeconds

        // Set timer duration and state
        _remainingTimeInSeconds.value = durationInSeconds
        _isTimerRunning.value = true
        onTimerComplete = onComplete

        // Start a new timer job
        timerJob = viewModelScope.launch {
            var lastTickTime = System.currentTimeMillis()

            while (_remainingTimeInSeconds.value > 0 && isActive) {
                delay(100) // More frequent updates for better accuracy

                // Calculate elapsed time since last tick
                val currentTime = System.currentTimeMillis()
                val elapsedMs = currentTime - lastTickTime

                // Only decrement if at least 1 second has passed
                if (elapsedMs >= 1000) {
                    _remainingTimeInSeconds.value -= 1
                    lastTickTime = currentTime

                    if (debug) println("Timer tick: ${_remainingTimeInSeconds.value} seconds remaining")
                }
            }

            if (_remainingTimeInSeconds.value <= 0 && isActive) {
                if (debug) println("Timer completed")
                _isTimerRunning.value = false
                onTimerComplete?.invoke()
            }
        }
    }

    fun pauseTimer() {
        if (debug) println("Pausing timer at: ${_remainingTimeInSeconds.value} seconds")

        stopTimerJob()
        _isTimerRunning.value = false
        // Don't reset remaining time here - this is crucial
    }

    fun resumeTimer() {
        if (_remainingTimeInSeconds.value > 0) {
            if (debug) println("Resuming timer with ${_remainingTimeInSeconds.value} seconds remaining")

            _isTimerRunning.value = true

            timerJob = viewModelScope.launch {
                var lastTickTime = System.currentTimeMillis()

                while (_remainingTimeInSeconds.value > 0 && isActive) {
                    delay(100)

                    val currentTime = System.currentTimeMillis()
                    val elapsedMs = currentTime - lastTickTime

                    if (elapsedMs >= 1000) {
                        _remainingTimeInSeconds.value -= 1
                        lastTickTime = currentTime

                        if (debug) println("Timer tick (resumed): ${_remainingTimeInSeconds.value} seconds remaining")
                    }
                }

                if (_remainingTimeInSeconds.value <= 0 && isActive) {
                    if (debug) println("Timer completed after resume")
                    _isTimerRunning.value = false
                    onTimerComplete?.invoke()
                }
            }
        } else {
            if (debug) println("Cannot resume timer - no time remaining")
        }
    }

    /**
     * Resets timer to its original duration without starting it
     */
    fun resetTimer() {
        if (debug) println("Resetting timer to $originalDurationInSeconds seconds (without starting)")

        stopTimerJob()
        _remainingTimeInSeconds.value = originalDurationInSeconds
        _isTimerRunning.value = false
    }

    fun stopTimer() {
        if (debug) println("Stopping timer")

        stopTimerJob()
        _isTimerRunning.value = false
        _remainingTimeInSeconds.value = 0
        onTimerComplete = null
    }

    private fun stopTimerJob() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimerJob()
    }
}