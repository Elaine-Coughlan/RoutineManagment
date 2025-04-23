package ie.setu.elaine.ui.screen.routine

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
import ie.setu.elaine.model.Routine
import ie.setu.elaine.viewmodel.RoutineViewModel

/**
 * Screen for creating or editing a routine.
 * Handles both creation of new routines and editing of existing ones.
 *
 * @param routineId ID of the routine to edit, or null if creating a new routine
 * @param viewModel RoutineViewModel for data operations
 * @param onSave Callback for when the routine is saved
 * @param onCancel Callback for when editing is canceled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutineScreen(
    routineId: String?,
    viewModel: RoutineViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // Determine if we're editing an existing routine or creating a new one
    val isEditing = routineId != null
    val routine = if (isEditing) {
        // Find existing routine by ID or create an empty one if not found
        viewModel.routines.firstOrNull { it.id == routineId } ?: Routine(title = "")
    } else {
        // Create a new empty routine
        Routine(title = "")
    }

    // Local state for form fields
    var title by remember { mutableStateOf(routine.title) }
    var description by remember { mutableStateOf(routine.description) }
    var isTimerEnabled by remember { mutableStateOf(routine.isTimerEnabled) }
    var totalDurationMinutes by remember { mutableStateOf(routine.totalDurationMinutes.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Routine" else "New Routine") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    // Save button - enabled only if title is not blank
                    IconButton(
                        onClick = {
                            // Create updated routine object
                            val updatedRoutine = routine.copy(
                                title = title,
                                description = description,
                                isTimerEnabled = isTimerEnabled,
                                totalDurationMinutes = totalDurationMinutes.toIntOrNull() ?: 0
                            )

                            // Either update existing or add new routine
                            if (isEditing) {
                                viewModel.updateRoutine(updatedRoutine)
                            } else {
                                viewModel.addRoutine(updatedRoutine)
                            }

                            // Navigate back using the provided callback
                            onSave()
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        // Form content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title field - required
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Routine Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description field - optional
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timer toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isTimerEnabled,
                    onCheckedChange = { isTimerEnabled = it }
                )

                Text("Enable Routine Timer")
            }

            // Duration field - only shown if timer is enabled
            if (isTimerEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = totalDurationMinutes,
                    onValueChange = { totalDurationMinutes = it },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}