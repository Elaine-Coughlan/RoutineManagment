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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ie.setu.elaine.R
import ie.setu.elaine.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: RoutineViewModel,
    onNavigateBack: () -> Unit
) {
    val currentRoutine = viewModel.currentRoutine.value
    val currentTask = viewModel.currentTask.value
    val taskTimeRemaining = viewModel.remainingTaskTimeInSeconds.value
    val isTaskTimerRunning = viewModel.currentTaskTimerRunning.value

    val minutes = taskTimeRemaining / 60
    val seconds = taskTimeRemaining % 60

    val iconRes = if (isTaskTimerRunning) R.drawable.video_pause_button else R.drawable.video_play_button


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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Task name
            Text(
                text = currentTask?.title ?: "No task selected",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Timer display
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 48.dp)
            )

            // Timer controls
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Play/Pause button
                IconButton(
                    onClick = {
                        if (isTaskTimerRunning) {
                            viewModel.pauseTimer()
                        } else {
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

                // Reset button
                IconButton(
                    onClick = {
                        currentRoutine?.id?.let { routineId ->
                            currentTask?.id?.let { taskId ->
                                viewModel.startTaskTimer(routineId, taskId)
                            }
                        }
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

            // Previous/Next buttons
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.moveToPreviousTask() },
                    enabled = currentRoutine?.tasks?.indexOf(currentTask) ?: 0 > 0
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Task")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Previous")
                }

                Button(
                    onClick = { viewModel.moveToNextTask() },
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