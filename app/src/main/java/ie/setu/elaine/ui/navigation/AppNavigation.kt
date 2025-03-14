package ie.setu.elaine.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ie.setu.elaine.ui.screen.routine.EditRoutineScreen
import ie.setu.elaine.ui.screen.routine.RoutineDetailScreen
import ie.setu.elaine.ui.screen.routine.RoutineListScreen
import ie.setu.elaine.ui.screen.task.EditTaskScreen
import ie.setu.elaine.viewmodel.RoutineViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: RoutineViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "routineList"
    ) {
        composable("routineList") {
            RoutineListScreen(
                viewModel = viewModel,
                onRoutineClick = { routineId ->
                    navController.navigate("routineDetail/$routineId")
                },
                onAddRoutineClick = {
                    navController.navigate("editRoutine")
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
    }
}