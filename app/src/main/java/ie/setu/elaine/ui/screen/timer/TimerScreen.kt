package ie.setu.elaine.ui.screen.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ie.setu.elaine.R
import ie.setu.elaine.viewmodel.RoutineViewModel

/**
 * Timer screen for tracking task duration within a routine
 *
 * This screen provides a timer interface with controls to start, pause, and reset
 * the timer for the current task. It also allows navigation between tasks in a routine.
 *
 * @param viewModel The view model containing timer and routine data
 * @param onNavigateBack Callback function to handle navigation back to previous screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: RoutineViewModel,
    onNavigateBack: () -> Unit
) {
    // Get current routine, task, and timer state from view model
    val currentRoutine = viewModel.currentRoutine.value
    val currentTask = viewModel.currentTask.value
    val taskTimeRemaining = viewModel.remainingTaskTimeInSeconds.value
    val isTaskTimerRunning = viewModel.currentTaskTimerRunning.value

    // Calculate minutes and seconds for display
    val minutes = taskTimeRemaining / 60
    val seconds = taskTimeRemaining % 60

    // Select the appropriate icon resource based on timer state
    val iconRes = if (isTaskTimerRunning) R.drawable.video_pause_button else R.drawable.video_play_button

    // Debug state to track user interactions
    var lastAction by remember { mutableStateOf("None") }

    // Log timer state changes for debugging
    LaunchedEffect(isTaskTimerRunning) {
        println("Timer running state changed: $isTaskTimerRunning")
    }

    // Main scaffold layout with top app bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentRoutine?.title ?: "Timer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Main column with timer display and controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display current task title
            Text(
                text = currentTask?.title ?: "No task selected",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Timer display in MM:SS format
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 48.dp)
            )

            // TODO: Remove - Debug information display
            Text(
                text = "Last action: $lastAction | Timer running: $isTaskTimerRunning",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Timer control buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Play/Pause button with dynamic icon
                IconButton(
                    onClick = {
                        if (isTaskTimerRunning) {
                            lastAction = "Pause pressed"
                            println("Pause button clicked")
                            viewModel.pauseTimer()
                        } else {
                            lastAction = "Resume pressed"
                            println("Resume button clicked")
                            viewModel.resumeTimer()
                        }
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = if (isTaskTimerRunning) "Pause" else "Play",
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Reset timer button
                IconButton(
                    onClick = {
                        lastAction = "Reset pressed"
                        println("Reset button clicked")
                        viewModel.resetTaskTimer() // Reset timer without starting
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Task navigation buttons (Previous/Next)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Previous task button (disabled if on first task)
                Button(
                    onClick = {
                        lastAction = "Previous task"
                        viewModel.moveToPreviousTask()
                    },
                    enabled = currentRoutine?.tasks?.indexOf(currentTask) ?: 0 > 0
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Task")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Previous")
                }

                // Next task button (disabled if on last task)
                Button(
                    onClick = {
                        lastAction = "Next task"
                        viewModel.moveToNextTask()
                    },
                    enabled = (currentRoutine?.tasks?.indexOf(currentTask) ?: 0) <
                            (currentRoutine?.tasks?.size ?: 1) - 1
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Task")
                }
            }
        }
    }
}