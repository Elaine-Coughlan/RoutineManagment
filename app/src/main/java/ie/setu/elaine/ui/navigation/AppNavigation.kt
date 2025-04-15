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
import ie.setu.elaine.ui.screen.routine.EditRoutineScreen
import ie.setu.elaine.ui.screen.routine.RoutineDetailScreen
import ie.setu.elaine.ui.screen.routine.RoutineListScreen
import ie.setu.elaine.ui.screen.task.EditTaskScreen
import ie.setu.elaine.ui.screen.timer.TimerScreen
import ie.setu.elaine.viewmodel.AchievementViewModel
import ie.setu.elaine.viewmodel.AchievementViewModelFactory
import ie.setu.elaine.viewmodel.RoutineViewModel
import ie.setu.elaine.viewmodel.RoutineViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: RoutineViewModel = viewModel(
        factory = RoutineViewModelFactory(context.applicationContext as Application)
    )
    val achievementViewModel: AchievementViewModel = viewModel(
        factory = AchievementViewModelFactory(context.applicationContext as Application)
    )


    NavHost(
        navController = navController,
        startDestination = "splashScreen"
    ) {
        composable("splashScreen"){
            SplashScreen(
                navController = navController

            )
        }

        composable("timer") {
            TimerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.pauseTimer()
                    navController.popBackStack()
                }
            )
        }

        composable("routineList") {
            RoutineListScreen(
                viewModel = viewModel,
                onRoutineClick = { routineId ->
                    navController.navigate("routineDetail/$routineId")
                },
                onAddRoutineClick = {
                    navController.navigate("editRoutine")
                },

                onAchievementsClick = {
                    navController.navigate("achievements")
                }
            )
        }

        composable(
            "routineDetail/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""

            RoutineDetailScreen(
                routineId = routineId,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onEditRoutine = { id ->
                    navController.navigate("editRoutine?routineId=$id")
                },
                onAddTask = { id ->
                    navController.navigate("editTask/$id")
                },
                onTaskClick = { routineId, taskId ->
                    navController.navigate("editTask/$routineId?taskId=$taskId")
                },
                onStartRoutine = {
                    navController.navigate("timer")
                }

            )
        }

        composable(
            "editRoutine?routineId={routineId}",
            arguments = listOf(navArgument("routineId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
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

        composable("achievements") {
            AchievementScreen(
                viewModel = achievementViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }



    }
}