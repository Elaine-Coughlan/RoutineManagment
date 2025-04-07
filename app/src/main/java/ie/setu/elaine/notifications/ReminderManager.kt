package ie.setu.elaine.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {

    companion object {
        const val DAILY_REMINDER_WORK = "daily_reminder_work"
        const val PREF_NAME = "elaine_app_prefs"
        const val LAST_OPENED_KEY = "last_app_open_time"
    }

    fun scheduleDailyReminder() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    fun recordAppOpened() {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putLong(LAST_OPENED_KEY, System.currentTimeMillis())
            apply()
        }
    }
}