package ie.setu.elaine.ui.screen.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import ie.setu.elaine.model.Routine
import ie.setu.elaine.viewmodel.RoutineViewModel
import ie.setu.elaine.R

/**
 * Screen that displays a list of all routines.
 * This is the main entry point for the routine feature.
 *
 * @param viewModel RoutineViewModel for accessing routine data
 * @param onRoutineClick Callback for when a routine is clicked
 * @param onAddRoutineClick Callback for when the add routine button is clicked
 * @param onAchievementsClick Callback for when the achievements button is clicked
 * @param onProgressClick Callback for when a routine's progress button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
    viewModel: RoutineViewModel,
    onRoutineClick: (String) -> Unit,
    onAddRoutineClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onProgressClick: (String) -> Unit, // Accepts routineId
) {
    val routines = viewModel.routines

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Routines") },
                actions = {
                    // Add routine button
                    IconButton(onClick = onAddRoutineClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Routine")
                    }

                    // Achievements button
                    IconButton(onClick = onAchievementsClick) {
                        Icon(
                            painter = painterResource(R.drawable.outline_trophy_24),
                            contentDescription = "Achievements"
                        )
                    }
                }
            )
        }
    ) { padding ->
        // List of routines
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(routines) { routine ->
                RoutineCard(
                    routine = routine,
                    onClick = { onRoutineClick(routine.id) },
                    onProgressClick = { onProgressClick(routine.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Composable for displaying a single routine card in the list.
 *
 * @param routine The routine to display
 * @param onClick Callback for when the card is clicked
 * @param onProgressClick Callback for when the progress button is clicked
 */
@Composable
fun RoutineCard(
    routine: Routine,
    onClick: () -> Unit,
    onProgressClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Routine title
            Text(
                text = routine.title,
                style = MaterialTheme.typography.headlineSmall
            )

            // Optional description
            if (routine.description.isNotEmpty()) {
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer row with task count, timer info, and progress button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Task count
                Text(
                    text = "${routine.tasks.size} tasks",
                    style = MaterialTheme.typography.labelSmall
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Timer info (if enabled)
                    if (routine.isTimerEnabled) {
                        Icon(
                            painter = painterResource(R.drawable.sand_clock),
                            contentDescription = "Timer",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${routine.totalDurationMinutes} min",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // Progress button
                    IconButton(
                        onClick = onProgressClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.sharp_query_stats_24),
                            contentDescription = "View Progress",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}