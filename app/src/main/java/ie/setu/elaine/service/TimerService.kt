package ie.setu.elaine.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerService {
    private var timerJob: Job? = null
    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun startTimer(durationInSeconds: Int, onTick: (Int) -> Unit, onFinish: () -> Unit) {
        stopTimer()

        _timeRemaining.value = durationInSeconds
        _isRunning.value = true

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (_timeRemaining.value > 0 && isActive) {
                delay(1000)
                _timeRemaining.value -= 1
                onTick(_timeRemaining.value)

                if (_timeRemaining.value <= 0) {
                    _isRunning.value = false
                    onFinish()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _isRunning.value = false
    }

    fun resumeTimer(onTick: (Int) -> Unit, onFinish: () -> Unit) {
        if (_timeRemaining.value > 0) {
            _isRunning.value = true

            timerJob = CoroutineScope(Dispatchers.Default).launch {
                while (_timeRemaining.value > 0 && isActive) {
                    delay(1000)
                    _timeRemaining.value -= 1
                    onTick(_timeRemaining.value)

                    if (_timeRemaining.value <= 0) {
                        _isRunning.value = false
                        onFinish()
                        break
                    }
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _isRunning.value = false
        _timeRemaining.value = 0
    }
}