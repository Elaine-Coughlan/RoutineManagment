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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    routineId: String,
    taskId: String?,
    viewModel: RoutineViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val routine = viewModel.routines.firstOrNull { it.id == routineId }

    val isEditing = taskId != null
    val task = if (isEditing && routine != null) {
        routine.tasks.firstOrNull { it.id == taskId } ?: Task(title = "")
    } else {
        Task(title = "")
    }

    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var isTimerEnabled by remember { mutableStateOf(task.isTimerEnabled) }
    var durationMinutes by remember { mutableStateOf(task.durationMinutes.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Task" else "New Task") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (routine != null) {
                                val updatedTask = task.copy(
                                    title = title,
                                    description = description,
                                    isTimerEnabled = isTimerEnabled,
                                    durationMinutes = durationMinutes.toIntOrNull() ?: 0
                                )

                                if (isEditing) {
                                    // Update existing task
                                    val taskIndex = routine.tasks.indexOfFirst { it.id == taskId }
                                    if (taskIndex >= 0) {
                                        val updatedTasks = routine.tasks.toMutableList()
                                        updatedTasks[taskIndex] = updatedTask
                                        viewModel.updateRoutine(routine.copy(tasks = updatedTasks))
                                    }
                                } else {
                                    // Add new task
                                    viewModel.addTaskToRoutine(routineId, updatedTask)
                                }

                                onSave()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
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
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

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