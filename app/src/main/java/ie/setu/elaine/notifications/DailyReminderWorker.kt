package ie.setu.elaine.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val PREF_NAME = "app_prefs"
        const val LAST_OPENED_KEY = "last_app_open_time"
    }

    override fun doWork(): Result {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastOpenedTime = sharedPrefs.getLong(LAST_OPENED_KEY, 0)
        val currentTime = System.currentTimeMillis()

        // Check if app hasn't been opened in the last 24 hours
        if (lastOpenedTime == 0L || currentTime - lastOpenedTime > TimeUnit.DAYS.toMillis(1)) {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showDailyReminder()
        }

        return Result.success()
    }
}