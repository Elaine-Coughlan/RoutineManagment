package ie.setu.elaine.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ie.setu.elaine.ui.components.SplashScreen
import ie.setu.elaine.ui.screen.achievement.AchievementScreen
import ie.setu.elaine.ui.screen.achievement.StreakScreen
import ie.setu.elaine.ui.screen.routine.EditRoutineScreen
import ie.setu.elaine.ui.screen.routine.RoutineDetailScreen
import ie.setu.elaine.ui.screen.routine.RoutineListScreen
import ie.setu.elaine.ui.screen.task.EditTaskScreen
import ie.setu.elaine.ui.screen.timer.TimerScreen
import ie.setu.elaine.viewmodel.AchievementViewModel
import ie.setu.elaine.viewmodel.AchievementViewModelFactory
import ie.setu.elaine.viewmodel.RoutineViewModel
import ie.setu.elaine.viewmodel.RoutineViewModelFactory

/**
 * Main navigation component for the application
 *
 * This composable defines the navigation graph for the entire app,
 * setting up routes between different screens and handling navigation arguments.
 * It also initializes the shared ViewModels used across multiple screens.
 */
@Composable
fun AppNavigation() {
    // Create navigation controller to handle navigation between screens
    val navController = rememberNavController()

    // Get application context for creating ViewModels
    val context = LocalContext.current

    // Initialize shared ViewModels using their respective factories
    val viewModel: RoutineViewModel = viewModel(
        factory = RoutineViewModelFactory(context.applicationContext as Application)
    )
    val achievementViewModel: AchievementViewModel = viewModel(
        factory = AchievementViewModelFactory(context.applicationContext as Application)
    )

    // Define the navigation host with routes to all screens
    NavHost(
        navController = navController,
        startDestination = "splashScreen"
    ) {
        // Splash screen - entry point of the app
        composable("splashScreen"){
            SplashScreen(
                navController = navController
            )
        }

        // Timer screen for tracking routine/task timers
        composable("timer") {
            TimerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    // Pause the timer before navigating back
                    viewModel.pauseTimer()
                    navController.popBackStack()
                }
            )
        }

        // Routine list screen - main screen showing all routines
        composable("routineList") {
            RoutineListScreen(
                viewModel = viewModel,
                onRoutineClick = { routineId ->
                    // Navigate to routine details when a routine is clicked
                    navController.navigate("routineDetail/$routineId")
                },
                onAddRoutineClick = {
                    // Navigate to edit screen to create a new routine
                    navController.navigate("editRoutine")
                },
                onAchievementsClick = {
                    // Navigate to achievements screen
                    navController.navigate("achievements")
                },
                onProgressClick = { routineId ->
                    // Navigate to streak progress for a specific routine
                    navController.navigate("streakProgress/$routineId")
                }
            )
        }

        // Routine detail screen showing tasks within a routine
        composable(
            "routineDetail/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Extract routine ID from navigation arguments
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""

            RoutineDetailScreen(
                routineId = routineId,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onEditRoutine = { id ->
                    // Navigate to edit routine with the routine ID
                    navController.navigate("editRoutine?routineId=$id")
                },
                onAddTask = { id ->
                    // Navigate to create a new task for this routine
                    navController.navigate("editTask/$id")
                },
                onTaskClick = { routineId, taskId ->
                    // Navigate to edit an existing task
                    navController.navigate("editTask/$routineId?taskId=$taskId")
                },
                onStartRoutine = {
                    // Navigate to timer screen to start the routine
                    navController.navigate("timer")
                }
            )
        }

        // Edit routine screen - handles both creating new and editing existing routines
        composable(
            "editRoutine?routineId={routineId}",
            arguments = listOf(navArgument("routineId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            // Extract optional routine ID (null when creating a new routine)
            val routineId = backStackEntry.arguments?.getString("routineId")

            EditRoutineScreen(
                routineId = routineId,
                viewModel = viewModel,
                onSave = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // Edit task screen - handles both creating new and editing existing tasks
        composable(
            "editTask/{routineId}?taskId={taskId}",
            arguments = listOf(
                navArgument("routineId") { type = NavType.StringType },
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // Extract routine ID and optional task ID
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
            val taskId = backStackEntry.arguments?.getString("taskId")

            EditTaskScreen(
                routineId = routineId,
                taskId = taskId,
                viewModel = viewModel,
                onSave = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // Achievements screen showing all user achievements
        composable("achievements") {
            AchievementScreen(
                viewModel = achievementViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Streak progress screen showing streak data for a specific routine
        composable(
            route = "streakProgress/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Extract routine ID for showing streak progress
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
            StreakScreen(
                routineId = routineId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}