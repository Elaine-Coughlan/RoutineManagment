package ie.setu.elaine.ui.screen.achievement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import ie.setu.elaine.R
import ie.setu.elaine.model.Achievement
import ie.setu.elaine.ui.components.AchievementCard
import ie.setu.elaine.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen( viewModel: RoutineViewModel, onNavigateBack: () -> Unit){
    val achievements = listOf(
        Achievement("First Routine", "Created your first routine", true),
        Achievement("Task Master", "Completed 10 tasks", false),
        Achievement("Consistency", "Used the app for 7 days in a row", true),
        Achievement("Time Keeper", "Completed a timed routine", false),
        Achievement("Organization Pro", "Created 5 routines", false)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Achievements")},
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
                items(achievements) {
                    achievement ->
                        AchievementCard(achievement = achievement)
                        Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }



