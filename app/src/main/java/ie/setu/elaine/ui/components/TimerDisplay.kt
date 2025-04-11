package ie.setu.elaine.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ie.setu.elaine.R

@Composable
fun TimerDisplay(
    timeInSeconds: Int,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60

    val iconRes = if (isRunning) R.drawable.video_pause_button else R.drawable.video_play_button

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = if (isRunning) onPause else onStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = if (isRunning) "Pause" else "Start"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isRunning) "Pause" else "Start")
                }

                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(painter = painterResource(R.drawable.stop_button), contentDescription = "Stop")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop")
                }
            }
        }
    }
}