package ie.setu.elaine.achievements

import android.app.Application
import ie.setu.elaine.RMApplication
import ie.setu.elaine.data.repository.AchievementRepository
import ie.setu.elaine.notifications.ReminderManager
import ie.setu.elaine.viewmodel.AchievementViewModel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope


class AchievementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var application: Application
    private lateinit var rmApplication: RMApplication
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var reminderManager: ReminderManager
    private lateinit var viewModel: AchievementViewModel

}