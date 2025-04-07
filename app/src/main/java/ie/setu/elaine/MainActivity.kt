package ie.setu.elaine

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import ie.setu.elaine.notifications.ReminderManager
import ie.setu.elaine.ui.navigation.AppNavigation
import ie.setu.elaine.ui.theme.RoutineManagementTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Record that the app was opened
        val reminderManager = (application as RMApplication).reminderManager
        reminderManager.recordAppOpened()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        setContent {
            RoutineManagementTheme {
                AppNavigation()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RoutineManagementTheme {
        // Preview content
    }
}