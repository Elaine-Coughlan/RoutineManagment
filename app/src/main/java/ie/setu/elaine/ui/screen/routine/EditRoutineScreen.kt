package ie.setu.elaine.ui.screen.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ie.setu.elaine.model.NotificationType
import ie.setu.elaine.model.Routine
import ie.setu.elaine.viewmodel.ReminderViewModel
import ie.setu.elaine.viewmodel.RoutineViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutineScreen(
    routineId: String?,
    viewModel: RoutineViewModel,
    reminderViewModel: ReminderViewModel, // Add this parameter
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = routineId != null
    val routine = if (isEditing) {
        viewModel.routines.firstOrNull { it.id == routineId } ?: Routine(title = "")
    } else {
        Routine(title = "")
    }

    var title by remember { mutableStateOf(routine.title) }
    var description by remember { mutableStateOf(routine.description) }
    var isTimerEnabled by remember { mutableStateOf(routine.isTimerEnabled) }
    var totalDurationMinutes by remember { mutableStateOf(routine.totalDurationMinutes.toString()) }

    // New reminder-related state
    var hasReminder by remember { mutableStateOf(routine.hasReminder) }
    var reminderTime by remember { mutableStateOf(routine.reminderTime ?: LocalTime.of(8, 0)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(routine.reminderDays) }
    var notificationType by remember { mutableStateOf(routine.notificationType) }

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
                    IconButton(
                        onClick = {
                            val updatedRoutine = routine.copy(
                                title = title,
                                description = description,
                                isTimerEnabled = isTimerEnabled,
                                totalDurationMinutes = totalDurationMinutes.toIntOrNull() ?: 0,
                                hasReminder = hasReminder,
                                reminderTime = if (hasReminder) reminderTime else null,
                                reminderDays = selectedDays,
                                notificationType = notificationType
                            )

                            if (isEditing) {
                                viewModel.updateRoutine(updatedRoutine)
                            } else {
                                viewModel.addRoutine(updatedRoutine)
                            }

                            // Handle reminder
                            if (hasReminder) {
                                reminderViewModel.createReminder(
                                    title = title,
                                    description = "Routine: $title",
                                    time = reminderTime,
                                    repeatDays = selectedDays,
                                    routineId = updatedRoutine.id,
                                    notificationType = notificationType
                                )
                            } else if (isEditing && routine.hasReminder) {
                                // Delete existing reminder if toggled off
                                routine.id.let { reminderViewModel.deleteReminder(it) }
                            }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Existing fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Routine Title") },
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

                Text("Enable Routine Timer")
            }

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

            // New reminder section
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Reminder Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasReminder,
                    onCheckedChange = { hasReminder = it }
                )

                Text("Set Reminder for This Routine")
            }

            if (hasReminder) {
                Spacer(modifier = Modifier.height(16.dp))

                // Time picker button
                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Call, //TODO replace call icon with alarm
                        contentDescription = "Set Time"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reminder Time: ${reminderTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                }

                if (showTimePicker) {
                    TimePickerDialog(
                        initialTime = reminderTime,
                        onTimeSelected = {
                            reminderTime = it
                            showTimePicker = false
                        },
                        onDismiss = { showTimePicker = false }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day selector
                Text(
                    text = "Repeat on days:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                DaySelector(
                    selectedDays = selectedDays,
                    onDaySelected = { day, selected ->
                        selectedDays = if (selected) {
                            selectedDays + day
                        } else {
                            selectedDays - day
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notification type
                Text(
                    text = "Notification Priority:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = notificationType == NotificationType.STANDARD,
                        onClick = { notificationType = NotificationType.STANDARD }
                    )
                    Text("Standard")

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = notificationType == NotificationType.URGENT,
                        onClick = { notificationType = NotificationType.URGENT }
                    )
                    Text("Urgent")

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = notificationType == NotificationType.SILENT,
                        onClick = { notificationType = NotificationType.SILENT }
                    )
                    Text("Silent")
                }
            }
        }
    }
}

// Helper composables
@Composable
fun DaySelector(
    selectedDays: List<DayOfWeek>,
    onDaySelected: (DayOfWeek, Boolean) -> Unit
) {
    val days = DayOfWeek.values()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            val isSelected = selectedDays.contains(day)
            DayButton(
                day = day,
                isSelected = isSelected,
                onClick = { onDaySelected(day, !isSelected) }
            )
        }
    }
}

@Composable
fun DayButton(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.name.first().toString(),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(initialTime.hour) }
    var minute by remember { mutableStateOf(initialTime.minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set reminder time") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simple number picker for hour
                    NumberPicker(
                        value = hour,
                        onValueChange = { hour = it },
                        range = 0..23
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Simple number picker for minute
                    NumberPicker(
                        value = minute,
                        onValueChange = { minute = it },
                        range = 0..59
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(LocalTime.of(hour, minute))
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                if (value < range.last) onValueChange(value + 1)
                else onValueChange(range.first)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
        }

        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium
        )

        IconButton(
            onClick = {
                if (value > range.first) onValueChange(value - 1)
                else onValueChange(range.last)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
        }
    }
}