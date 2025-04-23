package ie.setu.elaine.ui.screen.achievement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ie.setu.elaine.model.Streak
import ie.setu.elaine.ui.components.StreakCalendarView
import ie.setu.elaine.ui.components.StreakProgressCard
import ie.setu.elaine.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakScreen(
    routineId: String,
    viewModel: RoutineViewModel,
    onNavigateBack: () -> Unit
) {
    val currentRoutine by remember { mutableStateOf(viewModel.routines.find { it.id == routineId }) }
    val streak by viewModel.currentStreak
    val completionRecords = viewModel.completionRecords

    // Load streak data when screen is shown
    LaunchedEffect(routineId) {
        viewModel.loadStreakData(routineId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentRoutine?.title ?: "Routine Progress") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (streak != null) {
                StreakProgressCard(
                    streak = streak!!,
                    onUseStreakSaver = {
                        viewModel.useStreakSaver(routineId)
                    },
                    onChangeGoal = { newGoal ->
                        viewModel.updateStreakGoal(routineId, newGoal)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Streak milestones and achievements
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Milestones & Achievements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        MilestonesList(streak = streak!!)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calendar view of completions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    StreakCalendarView(
                        completionRecords = completionRecords
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun MilestonesList(streak: Streak) {
    // Common habit formation milestones
    val milestones = listOf(
        Milestone(7, "One Week Streak", "Completed 7 consecutive days"),
        Milestone(21, "Habit Forming", "21 days is a great start for habit formation"),
        Milestone(30, "Monthly Dedication", "Completed a full month!"),
        Milestone(66, "Habit Mastery", "Research suggests 66 days to form stable habits"),
        Milestone(100, "Century Club", "Triple digits achievement")
    )

    Column {
        milestones.forEach { milestone ->
            MilestoneItem(
                milestone = milestone,
                isAchieved = streak.longestStreak >= milestone.days
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun MilestoneItem(
    milestone: Milestone,
    isAchieved: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isAchieved) {
                Icons.Filled.Star
            } else {
                Icons.Default.Star
            },
            contentDescription = if (isAchieved) "Achieved" else "Not Achieved",
            tint = if (isAchieved) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isAchieved) FontWeight.Bold else FontWeight.Normal,
                color = if (isAchieved) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                }
            )

            Text(
                text = milestone.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "${milestone.days} days",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isAchieved) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
        )
    }
}

data class Milestone(
    val days: Int,
    val title: String,
    val description: String
)