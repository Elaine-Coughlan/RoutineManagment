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

/**
 * ViewModel responsible for managing achievement data and operations
 *
 * This ViewModel provides access to the list of user achievements and handles
 * updating achievement statuses based on app usage metrics.
 *
 * @param application The application instance used to access the repository
 */
class AchievementViewModel(application: Application) : AndroidViewModel(application) {
    // Get achievement repository from application instance
    private val repository = (application as RMApplication).achievementRepository

    /**
     * StateFlow containing the list of all achievements
     *
     * This flow is cached for 5 seconds after the last subscriber unsubscribes
     * to avoid unnecessary reloads of achievement data during quick UI changes
     */
    val achievements: StateFlow<List<Achievement>> = repository.allAchievements
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Refreshes the achievement status based on current app usage metrics
     *
     * This method checks various metrics like routine count, task completion,
     * and streak data to update achievement status in the repository.
     */
    fun refreshAchievementStatus() {
        viewModelScope.launch {
            // TODO: These values should be fetched from actual user data
            val routineCount = 0
            val taskCompletedCount = 0
            val hasDoneTimedRoutine = false
            val consecutiveDays = 0

            // Check and update achievements based on metrics
            repository.checkAndUpdateAchievements(
                routineCount = routineCount,
                taskCompletedCount = taskCompletedCount,
                hasDoneTimedRoutine = hasDoneTimedRoutine,
                consecutiveDays = consecutiveDays
            )
        }
    }
}

/**
 * Factory for creating AchievementViewModel instances
 *
 * This factory ensures the ViewModel is created with the necessary application
 * context for accessing repositories and other app resources.
 *
 * @param application The application instance to pass to the ViewModel
 */
class AchievementViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AchievementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AchievementViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}