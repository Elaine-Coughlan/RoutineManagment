package ie.setu.elaine.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ie.setu.elaine.model.CompletionRecord
import ie.setu.elaine.model.Streak
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity

@Composable
fun StreakProgressCard(
    streak: Streak,
    onUseStreakSaver: () -> Unit,
    onChangeGoal: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoalDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${streak.currentStreak} days",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ProgressBar(
                progress = streak.progress(),
                goal = streak.streakGoal
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Longest Streak",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${streak.longestStreak} days",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Total Completions",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${streak.daysCompleted} days",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Goal",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${streak.streakGoal} days",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { showGoalDialog = true },
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (streak.canUseStreakSaver()) {
                OutlinedButton(
                    onClick = onUseStreakSaver,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Use streak saver",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Streak Saver")
                }
            }
        }
    }

    if (showGoalDialog) {
        ChangeGoalDialog(
            currentGoal = streak.streakGoal,
            onConfirm = { newGoal ->
                onChangeGoal(newGoal)
                showGoalDialog = false
            },
            onDismiss = { showGoalDialog = false }
        )
    }
}

@Composable
fun ProgressBar(
    progress: Float,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progress"
    )

    var boxWidth by remember { mutableStateOf(0) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .onGloballyPositioned { coordinates ->
                    // Get the width in pixels
                    boxWidth = coordinates.size.width
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Show milestone markers
            val milestones = listOf(21, 30, 66)
            val density = LocalDensity.current

            Box(modifier = Modifier.fillMaxWidth()) {
                milestones.forEach { milestone ->
                    if (milestone <= goal) {
                        val position = milestone.toFloat() / goal.toFloat()

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = (position * boxWidth).toInt() - 4, // Adjust for dot size
                                        y = 0
                                    )
                                }
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (progress >= position)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "$goal days",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ChangeGoalDialog(
    currentGoal: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Streak Goal") },
        text = {
            Column {
                Text("Choose a new goal for your streak (in days):")

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = {
                        goalText = it
                        isError = it.toIntOrNull() == null || it.toIntOrNull() ?: 0 <= 0
                    },
                    label = { Text("Goal (days)") },
                    isError = isError,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    supportingText = {
                        if (isError) {
                            Text("Please enter a valid positive number")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Common goal options
                    listOf(21, 30, 66).forEach { days ->
                        SuggestionChip(
                            onClick = { goalText = days.toString() },
                            label = { Text("$days days") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    goalText.toIntOrNull()?.let { goal ->
                        if (goal > 0) {
                            onConfirm(goal)
                        }
                    }
                },
                enabled = !isError && goalText.toIntOrNull() != null
            ) {
                Text("Confirm")
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
fun StreakCalendarView(
    completionRecords: List<CompletionRecord>,
    modifier: Modifier = Modifier
) {
    val today = java.time.LocalDate.now()
    val monthStart = today.withDayOfMonth(1)
    val daysInMonth = today.month.length(today.isLeapYear)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = today.month.toString() + " " + today.year,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Week days header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar grid
        val firstDayOfWeek = monthStart.dayOfWeek.value % 7 // Convert to 0-6 where 0 is Sunday

        val weeks = (daysInMonth + firstDayOfWeek + 6) / 7 // Calculate how many weeks to display

        for (week in 0 until weeks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (day in 0 until 7) {
                    val dayOfMonth = week * 7 + day - firstDayOfWeek + 1

                    if (dayOfMonth in 1..daysInMonth) {
                        val date = monthStart.withDayOfMonth(dayOfMonth)
                        val isCompleted = completionRecords.any {
                            it.completedDate.equals(date)
                        }
                        val isStreakSaver = completionRecords.any {
                            it.completedDate.equals(date) && it.isStreakSaver
                        }

                        DayCircle(
                            day = dayOfMonth.toString(),
                            isCompleted = isCompleted,
                            isStreakSaver = isStreakSaver,
                            isToday = date.equals(today),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty space for days not in this month
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun DayCircle(
    day: String,
    isCompleted: Boolean,
    isStreakSaver: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    isCompleted && isStreakSaver -> MaterialTheme.colorScheme.tertiary
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.surfaceVariant
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

data class CompletionRecord(
    val id: Long = 0,
    val routineId: String,
    val completedDate: java.time.LocalDate,
    val isStreakSaver: Boolean = false
)