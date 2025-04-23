package ie.setu.elaine.ui.screen.achievement

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.elaine.ui.components.AchievementCard
import ie.setu.elaine.viewmodel.AchievementViewModel

/**
 * Achievement Screen that displays all user achievements
 *
 * This screen presents a scrollable list of achievements with their status (completed or not)
 * using the AchievementViewModel to manage the achievement data.
 *
 * @param viewModel The view model that contains achievement data and business logic
 * @param onNavigateBack Callback function to handle navigation back to previous screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    viewModel: AchievementViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect achievement state from ViewModel as state that re-composes on changes
    val achievements by viewModel.achievements.collectAsState()

    // Load achievement data when screen is first displayed
    LaunchedEffect(key1 = true) {
        viewModel.refreshAchievementStatus()
    }

    // Main scaffold layout with top app bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
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
    ) { padding ->
        // Scrollable list of achievements with padding from scaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Render each achievement as a card with spacing between items
            items(achievements) { achievement ->
                AchievementCard(achievement = achievement)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}