package ie.setu.elaine.ui.screen.routine

import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import ie.setu.elaine.model.Task
import ie.setu.elaine.viewmodel.RoutineViewModel
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routineId: String,
    viewModel: RoutineViewModel,
    onBack: () -> Unit,
    onEditRoutine: (String) -> Unit,
    onAddTask: (String) -> Unit,
    onTaskClick: (String, String) -> Unit,
    onStartRoutine: () -> Unit
) {
    val routine = viewModel.routines.firstOrNull { it.id == routineId }
    val navController = rememberNavController()

    routine?.let {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(routine.title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEditRoutine(routineId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Routine")
                        }
                        IconButton(onClick = { onAddTask(routineId) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Routine summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (routine.description.isNotEmpty()) {
                            Text(
                                text = routine.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total time: ${routine.totalDurationMinutes} min",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (routine.isTimerEnabled) {
                                Button(
                                    onClick = { viewModel.startRoutineTimer(routineId)
                                        onStartRoutine()}
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start Routine")
                                }
                            }

                            // Add to the routine summary card in RoutineDetailScreen
                            if (routine.hasReminder) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call, //TODO replace with Alarm icon
                                        contentDescription = "Reminder",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reminder: ${routine.reminderTime?.format(
                                            DateTimeFormatter.ofPattern("hh:mm a"))}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // Display which days the reminder repeats
                                Text(
                                    text = "Repeats on: ${routine.reminderDays.joinToString(", ") {
                                        it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                    }}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task list
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn {
                    items(routine.tasks) { task ->
                        TaskItem(
                            task = task,
                            onClick = { onTaskClick(routineId, task.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    } ?: run {
        // Handle case where routine is not found
        Text("Routine not found")
        Button(onClick = onBack) {
            Text("Go Back")
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.displaySmall
                )

                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (task.isTimerEnabled) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${task.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall
                    )

                    IconButton(
                        onClick = { /* Start task timer */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,  //TODO add timer icon
                            contentDescription = "Start Timer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}