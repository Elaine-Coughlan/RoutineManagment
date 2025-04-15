package ie.setu.elaine.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ie.setu.elaine.RMApplication
import ie.setu.elaine.data.local.entity.Achievement
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AchievementViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as RMApplication).achievementRepository

    val achievements: StateFlow<List<Achievement>> = repository.allAchievements
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun refreshAchievementStatus() {
        viewModelScope.launch {
            val routineCount = 0
            val taskCompletedCount = 0
            val hasDoneTimedRoutine = false
            val consecutiveDays = 0

            repository.checkAndUpdateAchievements(
                routineCount = routineCount,
                taskCompletedCount = taskCompletedCount,
                hasDoneTimedRoutine = hasDoneTimedRoutine,
                consecutiveDays = consecutiveDays
            )
        }
    }
}

class AchievementViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AchievementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AchievementViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}