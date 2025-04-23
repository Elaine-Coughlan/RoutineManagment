package ie.setu.elaine.ui.screen.task

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ie.setu.elaine.model.Task
import ie.setu.elaine.viewmodel.RoutineViewModel

/**
 * Screen for creating a new task or editing an existing task within a routine
 *
 * This screen provides input fields for task details and handles saving changes
 * to the task through the RoutineViewModel.
 *
 * @param routineId ID of the routine this task belongs to
 * @param taskId ID of the task to edit (null if creating a new task)
 * @param viewModel The view model containing routine and task data
 * @param onSave Callback function invoked when the task is saved
 * @param onCancel Callback function invoked when editing is cancelled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    routineId: String,
    taskId: String?,
    viewModel: RoutineViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // Find the routine this task belongs to
    val routine = viewModel.routines.firstOrNull { it.id == routineId }

    // Determine if editing an existing task or creating a new one
    val isEditing = taskId != null
    val task = if (isEditing && routine != null) {
        // Find existing task or create an empty one if not found
        routine.tasks.firstOrNull { it.id == taskId } ?: Task(title = "")
    } else {
        // Create a new empty task
        Task(title = "")
    }

    // Create state variables for all editable task fields
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var isTimerEnabled by remember { mutableStateOf(task.isTimerEnabled) }
    var durationMinutes by remember { mutableStateOf(task.durationMinutes.toString()) }

    // Main scaffold layout with top app bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Task" else "New Task") },
                navigationIcon = {
                    // Cancel button
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    // Save button
                    IconButton(
                        onClick = {
                            if (routine != null) {
                                // Create updated task with current input values
                                val updatedTask = task.copy(
                                    title = title,
                                    description = description,
                                    isTimerEnabled = isTimerEnabled,
                                    durationMinutes = durationMinutes.toIntOrNull() ?: 0
                                )

                                if (isEditing) {
                                    // Update existing task in the routine
                                    val taskIndex = routine.tasks.indexOfFirst { it.id == taskId }
                                    if (taskIndex >= 0) {
                                        val updatedTasks = routine.tasks.toMutableList()
                                        updatedTasks[taskIndex] = updatedTask
                                        viewModel.updateRoutine(routine.copy(tasks = updatedTasks))
                                    }
                                } else {
                                    // Add a new task to the routine
                                    viewModel.addTaskToRoutine(routineId, updatedTask)
                                }

                                // Navigate back after saving
                                onSave()
                            }
                        },
                        // Disable save button if title is empty
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        // Scrollable form with input fields
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Task title input field (required)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Optional task description input field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timer toggle checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isTimerEnabled,
                    onCheckedChange = { isTimerEnabled = it }
                )

                Text("Enable Task Timer")
            }

            // Timer duration input (only shown when timer is enabled)
            if (isTimerEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}